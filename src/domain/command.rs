use std::fmt::{Display, Formatter};
use std::fs;
use std::fs::File;
use std::io::Write;
use std::os::unix::prelude::PermissionsExt;
use std::path::{Path, PathBuf};
use std::process::{Command, Output};
use std::str::from_utf8;
use std::sync::atomic::{AtomicUsize, Ordering};
use std::time::{SystemTime, UNIX_EPOCH};

use once_cell::sync::Lazy;
use serde::Deserialize;

use crate::domain::{Context, MarkdownRunnable};
use crate::utils::paths;
use crate::utils::paths::current_dir;

#[derive(Debug, PartialEq, Deserialize)]
pub(crate) struct CommandEntry {
    commands: Vec<String>,
    on_failure_commands: Option<Vec<String>>,
    working_dir: Option<String>,
    output: Option<CommandOutput>,
    tags: Option<Vec<String>>,
}

impl CommandEntry {
    fn evaluate_current_dir(&self) -> PathBuf {
        self.working_dir
            .as_ref()
            .map_or_else(|| current_dir(), |path| current_dir().join(path.clone()))
    }

    fn format_shell_script(&self) -> String {
        let mut commands = String::new();
        commands.push_str(
            r#"#!/bin/sh

# Generated by the Sociable Weaver application
# This file is automatically deleted once the execution completes

set -e

"#,
        );
        commands.push_str(&self.commands.join("\n"));
        commands
    }

    fn evaluate_on_failure_commands(&self) -> Option<String> {
        self.on_failure_commands.as_ref().map(|on_failure_commands| {
            let mut commands = String::new();
            commands.push_str(
                r#"#!/bin/sh

# Generated by the Sociable Weaver application
# This file is automatically deleted once the execution completes
# All commands in the script are executed even when a previous command fails as this is intended for clean up

"#,
            );

            commands.push_str(&on_failure_commands.join("\n"));
            commands
        })
    }

    fn run_on_failure_commands(&self, current_dir: &Path) {
        self.evaluate_on_failure_commands()
            .inspect(|on_failure_commands| {
                let _ = ShellScript::new(current_dir, on_failure_commands).run();
            });
    }
}

#[derive(Debug, PartialEq, Deserialize)]
pub(crate) struct CommandOutput {
    #[serde(default = "CommandOutput::default_show_value")]
    show: bool,
    #[serde(default = "CommandOutput::default_caption_value")]
    caption: String,
}

impl CommandOutput {
    fn new() -> Self {
        CommandOutput {
            show: Self::default_show_value(),
            caption: Self::default_caption_value(),
        }
    }

    fn with_caption(caption: String) -> Self {
        CommandOutput {
            show: Self::default_show_value(),
            caption,
        }
    }

    fn default_caption_value() -> String {
        "Output:".to_string()
    }

    fn default_show_value() -> bool {
        true
    }
}

impl MarkdownRunnable for CommandEntry {
    fn to_markdown(&self, _context: &mut Context) -> Result<String, String> {
        let current_dir = self.evaluate_current_dir();
        let shell_script = self.format_shell_script();
        let result = ShellScript::new(&current_dir, &shell_script)
            .run()
            .inspect(|output| {
                if !output.status.success() {
                    self.run_on_failure_commands(&current_dir);
                }
            })
            .inspect_err(|_| {
                self.run_on_failure_commands(&current_dir);
            });

        match result {
            Ok(output) => {
                if output.status.success() {
                    let commands = self.commands.join("\n");

                    let mut markdown = String::new();
                    markdown.push_str("```shell\n");
                    if let Some(dir) = &self.working_dir {
                        markdown.push_str(&format!(
                            "# Running command from within the {} directory\n",
                            dir
                        ));
                        // TODO: Handle paths that have the ' in their name
                        markdown.push_str(&format!("(cd '{}'\n", dir));
                    }
                    markdown.push_str(commands.as_str());
                    markdown.push_str("\n");
                    if let Some(_) = &self.working_dir {
                        markdown.push_str(")\n");
                    }
                    markdown.push_str("```\n");

                    if let Some(command_output) = &self.output {
                        if command_output.show {
                            markdown.push_str("\n");
                            markdown.push_str(&command_output.caption);
                            markdown.push_str("\n\n```\n");
                            markdown.push_str(
                                from_utf8(&output.stdout).expect("Failed to get the output"),
                            );
                            markdown.push_str("```\n");
                        }
                    }

                    Ok(markdown)
                } else {
                    Err(from_utf8(&output.stderr)
                        .expect("Failed to get the error")
                        .to_string())
                }
            }

            Err(e) => Err(e.to_string()),
        }
    }
}

impl Display for CommandEntry {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        writeln!(f, "{}", &self.commands.join("\n"))
    }
}

struct ShellScript {
    path: PathBuf,
}

impl ShellScript {
    pub(crate) fn new(directory: &Path, commands: &str) -> Self {
        let script_path = Self::create_file_path(directory);

        Self::create_shell_script(&script_path)
            .write_all(commands.as_bytes())
            .expect("Failed to create shell script");

        ShellScript { path: script_path }
    }

    pub(crate) fn run(self) -> std::io::Result<Output> {
        Command::new("/bin/sh")
            .current_dir(paths::parent_dir(&self.path))
            .args(["-c", &paths::path_as_str(&self.path)])
            .output()
    }

    fn create_file_path(directory: &Path) -> PathBuf {
        static START_TIME: Lazy<u128> = Lazy::new(|| ShellScript::millis_since_epoch());
        let start_time = *START_TIME;

        static COUNTER: AtomicUsize = AtomicUsize::new(1);
        let index = COUNTER.fetch_add(1, Ordering::Relaxed);

        directory.join(format!(".sw-commands-{start_time}-{index}.sh"))
    }

    fn millis_since_epoch() -> u128 {
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .expect("Time went backwards")
            .as_millis()
    }

    fn create_shell_script(path: &Path) -> File {
        let shell_script = File::create(path).expect("Failed to create shell script");
        Self::make_shell_script_executable(&shell_script);
        shell_script
    }

    fn make_shell_script_executable(shell_script: &File) {
        let metadata = shell_script
            .metadata()
            .expect("Failed to get the script metadata");

        let mut permissions = metadata.permissions();
        permissions.set_mode(0o755);

        shell_script
            .set_permissions(permissions)
            .expect("Failed to set the script permissions");
    }
}

impl Drop for ShellScript {
    fn drop(&mut self) {
        if fs::remove_file(&self.path).is_err() {
            eprintln!("Failed to delete the shell script");
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    mod deserialized {
        use crate::domain::Document;
        use crate::domain::Entry::Command;

        use super::*;

        #[test]
        fn return_deserialized_command_when_given_minimum_options() {
            let json = r#"{
  "entries": [
    {
      "type": "Command",
      "commands": [
        "echo \"Hello there!\""
      ]
    }
  ]
}"#;

            let expected = Document {
                entries: vec![Command(CommandEntry {
                    commands: vec!["echo \"Hello there!\"".to_string()],
                    on_failure_commands: None,
                    working_dir: None,
                    output: None,

                    tags: None,
                })],
            };

            let deserialized: Document = Document::parse(json).unwrap();
            assert_eq!(expected, deserialized);
        }

        #[test]
        fn return_deserialized_command_when_given_all_options() {
            let json = r#"{
  "entries": [
    {
      "type": "Command",
      "commands": [
        "echo \"Hello ${NAME}!\""
      ],
      "on_failure_commands": [
        "echo \"Failed to say hello ${NAME}!\""
      ],
      "working_dir": "dir",
      "output": {
        "show": false,
        "caption": "The output is hidden"
      },
      "variables": [
        "NAME"
      ],
      "tags": [
        "test"
      ]
    }
  ]
}"#;

            let expected = Document {
                entries: vec![Command(CommandEntry {
                    commands: vec!["echo \"Hello ${NAME}!\"".to_string()],
                    on_failure_commands: Some(vec![
                        "echo \"Failed to say hello ${NAME}!\"".to_string()
                    ]),
                    working_dir: Some("dir".to_string()),
                    output: Some(CommandOutput {
                        show: false,
                        caption: "The output is hidden".to_string(),
                    }),
                    tags: Some(vec!["test".to_string()]),
                })],
            };

            let deserialized: Document = Document::parse(json).unwrap();
            assert_eq!(expected, deserialized);
        }
    }

    mod current_dir {
        use crate::utils::paths;

        use super::*;

        #[test]
        fn return_the_give_path_when_working_dir_is_missing() {
            let command = CommandEntry {
                commands: vec!["date".to_string()],
                on_failure_commands: None,
                working_dir: None,
                output: None,

                tags: None,
            };

            let current_dir = paths::current_dir();
            let result = command.evaluate_current_dir();
            assert_eq!(current_dir, result);
        }

        #[test]
        fn return_the_constructed_path_when_working_dir_is_relative() {
            let command = CommandEntry {
                commands: vec!["date".to_string()],
                on_failure_commands: None,
                working_dir: Some("test".to_string()),
                output: None,

                tags: None,
            };

            let current_dir = paths::current_dir();
            let result = command.evaluate_current_dir();
            assert_eq!(current_dir.join("test"), result);
        }

        #[test]
        fn return_the_working_dir_when_it_is_absolute() {
            let command = CommandEntry {
                commands: vec!["date".to_string()],
                on_failure_commands: None,
                working_dir: Some("/test".to_string()),
                output: None,

                tags: None,
            };

            let result = command.evaluate_current_dir();
            assert_eq!(PathBuf::from("/test"), result);
        }
    }

    // mod run {
    //     use std::str::from_utf8;
    //
    //     use crate::utils::paths;
    //
    //     use super::*;
    //
    //     #[test]
    //     fn execute_single_command() {
    //         let command = CommandEntry {
    //             commands: vec!["echo 'Hello World!'".to_string()],
    //             on_failure_commands: None,
    //             working_dir: None,
    //             output: None,
    //             tags: None,
    //         };
    //
    //         let mut context = Context {
    //             current_dir: paths::current_dir(),
    //         };
    //
    //         let result = command.run(&mut context);
    //         assert!(result.is_ok());
    //
    //         let output = result.unwrap();
    //         assert!(output.status.success());
    //         let echo = from_utf8(&output.stdout)
    //             .expect("Failed to parse stdout as UTF-8")
    //             .trim();
    //         assert_eq!("Hello World!", echo);
    //     }
    //
    //     #[test]
    //     fn execute_multiple_commands() {
    //         let command = CommandEntry {
    //             commands: vec!["echo 'Hello'".to_string(), "echo 'World!'".to_string()],
    //             on_failure_commands: None,
    //             working_dir: None,
    //             output: None,
    //             tags: None,
    //         };
    //
    //         let mut context = Context {
    //             current_dir: paths::current_dir(),
    //         };
    //
    //         let result = command.run(&mut context);
    //         assert!(result.is_ok());
    //
    //         let output = result.unwrap();
    //         assert!(output.status.success());
    //         let echo = from_utf8(&output.stdout)
    //             .expect("Failed to parse stdout as UTF-8")
    //             .trim();
    //         assert_eq!("Hello\nWorld!", echo);
    //     }
    //
    //     #[test]
    //     fn execute_command_from_different_working_dir() {
    //         let target = paths::current_dir().join("target");
    //
    //         let command = CommandEntry {
    //             commands: vec!["pwd".to_string()],
    //             on_failure_commands: None,
    //             working_dir: Some("target".to_string()),
    //             output: None,
    //             tags: None,
    //         };
    //
    //         let result = command.run(&mut Context::empty());
    //         assert!(result.is_ok());
    //
    //         let output = result.unwrap();
    //         assert!(output.status.success());
    //         let pwd = from_utf8(&output.stdout)
    //             .expect("Failed to parse stdout as UTF-8")
    //             .trim();
    //         assert_eq!(paths::path_as_str(&target), pwd);
    //     }
    //
    //     #[test]
    //     #[ignore]
    // }

    mod markdown_runnable_tests {
        use super::*;

        #[test]
        fn format_multiple_commands() {
            /* Given */
            let entry = CommandEntry {
                commands: vec!["echo 1".to_string(), "echo 2".to_string()],
                on_failure_commands: None,
                working_dir: None,
                output: None,
                tags: None,
            };

            /* When */
            let md = entry.to_markdown(&mut Context::empty());

            /* Then */
            assert_eq!(
                Ok(r#"```shell
echo 1
echo 2
```
"#
                .to_string()),
                md
            );
        }

        #[test]
        fn format_commands_in_working_dir() {
            /* Given */
            let entry = CommandEntry {
                commands: vec!["echo 1".to_string(), "echo 2".to_string()],
                on_failure_commands: None,
                working_dir: Some("target".to_string()),
                output: None,
                tags: None,
            };

            /* When */
            let md = entry.to_markdown(&mut Context::empty());

            /* Then */
            assert_eq!(
                Ok(r#"```shell
# Running command from within the target directory
(cd 'target'
echo 1
echo 2
)
```
"#
                .to_string()),
                md
            );
        }

        #[test]
        fn format_commands_and_show_output() {
            /* Given */
            let entry = CommandEntry {
                commands: vec!["echo 'Albert Attard'".to_string()],
                on_failure_commands: None,
                working_dir: None,
                output: Some(CommandOutput::with_caption(
                    "The command will print:".to_string(),
                )),

                tags: None,
            };

            /* When */
            let md = entry.to_markdown(&mut Context::empty());

            /* Then */
            assert_eq!(
                Ok(r#"```shell
echo 'Albert Attard'
```

The command will print:

```
Albert Attard
```
"#
                .to_string()),
                md
            );
        }

        #[test]
        fn format_error_when_command_fails() {
            let command = CommandEntry {
                commands: vec!["failing on purpose".to_string()],
                on_failure_commands: Some(vec![
                    "cat << EOF > './target/error.txt'".to_string(),
                    "It failed!".to_string(),
                    "EOF".to_string(),
                ]),
                working_dir: None,
                output: None,
                tags: None,
            };

            let result = command.to_markdown(&mut Context::empty());
            assert!(result.is_err());

            let error = result.err().unwrap();
            assert!(error.len() > 0);

            let error_message = fs::read_to_string("./target/error.txt");
            assert!(error_message.is_ok());
            assert_eq!(
                "It failed!\n",
                error_message
                    .expect("Failed to read the error message")
                    .as_str()
            );
        }
    }

    #[test]
    fn format_command_with_long_output() {
        let command = CommandEntry {
            commands: vec!["for i in {1..10000}; do echo \"[${i}] Hello World!\"; done".to_string()],
            on_failure_commands: None,
            working_dir: None,
            output: Some(CommandOutput::new()),
            tags: None,
        };

        let mut context = Context {
            current_dir: paths::current_dir(),
        };

        let result = command.to_markdown(&mut context);
        assert!(result.is_ok());

        let output = result.unwrap();

        let mut expected = String::new();
        expected.push_str("```shell\nfor i in {1..10000}; do echo \"[${i}] Hello World!\"; done\n```\n\nOutput:\n\n```\n");
        for i in 1..10001 {
            expected.push_str(&format!("[{}] Hello World!\n", i))
        }
        expected.push_str("```\n");
        assert_eq!(expected, output);
    }
}
