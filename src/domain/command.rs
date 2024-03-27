use std::fmt::{Display, Formatter};
use std::fs;
use std::fs::File;
use std::io::Write;
use std::os::unix::prelude::PermissionsExt;
use std::path::{Path, PathBuf};
use std::process::{Command, Output};
use std::sync::atomic::{AtomicUsize, Ordering};
use std::time::{SystemTime, UNIX_EPOCH};

use serde::Deserialize;

use crate::domain::{Context, Runnable};
use crate::utils::paths;

#[derive(Debug, PartialEq, Deserialize)]
pub(crate) struct CommandEntry {
    commands: Vec<String>,
    working_dir: Option<String>,
    variables: Option<Vec<String>>,
}

impl CommandEntry {
    fn variable_values(&self, context: &mut Context) -> Vec<(String, String)> {
        if let Some(v) = &self.variables {
            v.iter()
                .map(|name| (name, context.value(name)))
                .filter_map(|(name, value)| value.map(|v| (format!("$VAR[{}]", name), v)))
                .collect()
        } else {
            vec![]
        }
    }

    fn evaluate_current_dir<P: AsRef<Path>>(
        &self,
        current_dir: &P,
        variables_values: &Vec<(String, String)>,
    ) -> PathBuf {
        self.working_dir.as_ref().map_or_else(
            || current_dir.as_ref().to_path_buf(),
            |path| {
                current_dir
                    .as_ref()
                    .join(Self::evaluate(path.clone(), variables_values))
            },
        )
    }

    fn evaluate_commands(&self, variables_values: &Vec<(String, String)>) -> Vec<String> {
        self.commands
            .iter()
            .cloned()
            .map(|line| Self::evaluate(line, variables_values))
            .collect()
    }

    fn evaluate(mut line: String, variables_values: &Vec<(String, String)>) -> String {
        for var_val in variables_values {
            if line.contains(&var_val.0) {
                line = line.replace(&var_val.0, &var_val.1)
            }
        }
        line
    }
}

impl Runnable for CommandEntry {
    fn run(&self, context: &mut Context) -> std::io::Result<Output> {
        let variables_values = self.variable_values(context);
        let commands = self.evaluate_commands(&variables_values).join("\n");
        let current_dir = self.evaluate_current_dir(&context.current_dir, &variables_values);

        ShellScript::new(&current_dir, &commands).run()
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
        static COUNTER: AtomicUsize = AtomicUsize::new(1);
        let index = COUNTER.fetch_add(1, Ordering::Relaxed);
        directory.join(format!(
            "command-{}-{}.sh",
            index,
            Self::millis_since_epoch()
        ))
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
  "variables": [],
  "entries": [
    {
      "type": "Command",
      "commands": [
        "date"
      ]
    }
  ]
}"#;

            let expected = Document {
                variables: vec![],
                entries: vec![Command(CommandEntry {
                    commands: vec!["date".to_string()],
                    working_dir: None,
                    variables: None,
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
                working_dir: None,
                variables: None,
            };

            let current_dir = paths::current_dir();
            let result = command.evaluate_current_dir(&current_dir, &vec![]);
            assert_eq!(current_dir, result);
        }

        #[test]
        fn return_the_constructed_path_when_working_dir_is_relative() {
            let command = CommandEntry {
                commands: vec!["date".to_string()],
                working_dir: Some("test".to_string()),
                variables: None,
            };

            let current_dir = paths::current_dir();
            let result = command.evaluate_current_dir(&current_dir, &vec![]);
            assert_eq!(current_dir.join("test"), result);
        }

        #[test]
        fn return_the_working_dir_when_it_is_absolute() {
            let command = CommandEntry {
                commands: vec!["date".to_string()],
                working_dir: Some("/test".to_string()),
                variables: None,
            };

            let current_dir = paths::current_dir();
            let result = command.evaluate_current_dir(&current_dir, &vec![]);
            assert_eq!(PathBuf::from("/test"), result);
        }
    }

    mod run {
        use std::str::from_utf8;

        use crate::domain::ContextVariable;
        use crate::utils::paths;

        use super::*;

        #[test]
        fn execute_single_command() {
            let command = CommandEntry {
                commands: vec!["echo 'Hello World!'".to_string()],
                working_dir: None,
                variables: None,
            };

            let mut context = Context {
                current_dir: paths::current_dir(),
                variables: vec![],
            };

            let result = command.run(&mut context);
            assert!(result.is_ok());

            let output = result.unwrap();
            assert!(output.status.success());
            let echo = from_utf8(&output.stdout)
                .expect("Failed to parse stdout as UTF-8")
                .trim();
            assert_eq!("Hello World!", echo);
        }

        #[test]
        fn execute_multiple_commands() {
            let command = CommandEntry {
                commands: vec!["echo 'Hello'".to_string(), "echo 'World!'".to_string()],
                working_dir: None,
                variables: None,
            };

            let mut context = Context {
                current_dir: paths::current_dir(),
                variables: vec![],
            };

            let result = command.run(&mut context);
            assert!(result.is_ok());

            let output = result.unwrap();
            assert!(output.status.success());
            let echo = from_utf8(&output.stdout)
                .expect("Failed to parse stdout as UTF-8")
                .trim();
            assert_eq!("Hello\nWorld!", echo);
        }

        #[test]
        fn execute_command_that_contains_variables() {
            let command = CommandEntry {
                commands: vec!["echo 'Hello $VAR[NAME]!'".to_string()],
                working_dir: None,
                variables: Some(vec!["NAME".to_string()]),
            };

            let mut context = Context {
                current_dir: paths::current_dir(),
                variables: vec![ContextVariable {
                    name: "NAME".to_string(),
                    value: "Albert".to_string(),
                }],
            };

            let result = command.run(&mut context);
            assert!(result.is_ok());

            let output = result.unwrap();
            assert!(output.status.success());
            let echo = from_utf8(&output.stdout)
                .expect("Failed to parse stdout as UTF-8")
                .trim();
            assert_eq!("Hello Albert!", echo);
        }

        #[test]
        fn execute_command_from_different_working_dir() {
            let target = paths::current_dir().join("target");

            let command = CommandEntry {
                commands: vec!["pwd".to_string()],
                working_dir: Some("$VAR[PWD]".to_string()),
                variables: Some(vec!["PWD".to_string()]),
            };

            let mut context = Context {
                current_dir: paths::current_dir(),
                variables: vec![ContextVariable {
                    name: "PWD".to_string(),
                    value: "target".to_string(),
                }],
            };

            let result = command.run(&mut context);
            assert!(result.is_ok());

            let output = result.unwrap();
            assert!(output.status.success());
            let pwd = from_utf8(&output.stdout)
                .expect("Failed to parse stdout as UTF-8")
                .trim();
            assert_eq!(paths::path_as_str(&target), pwd);
        }
    }
}
