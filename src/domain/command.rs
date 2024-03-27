use std::fmt::{Display, Formatter};
use std::fs;
use std::path::{Path, PathBuf};
use std::process::{Command, Output};

use serde::Deserialize;

use crate::domain::{Context, Runnable};

#[derive(Debug, PartialEq, Deserialize)]
pub(crate) struct CommandEntry {
    command: String,
    working_dir: Option<PathBuf>,
    arguments: Option<Vec<String>>,
    variables: Option<Vec<String>>,
}

impl CommandEntry {
    fn current_dir<P: AsRef<Path>>(&self, current_dir: &P) -> PathBuf {
        self.working_dir.as_ref().map_or_else(
            || current_dir.as_ref().to_path_buf(),
            |path| current_dir.as_ref().join(path),
        )
    }

    fn variable_values(&self, context: &mut Context) -> Vec<(String, String)> {
        if let Some(v) = &self.variables {
            v.iter()
                .map(|name| (name, context.value(name)))
                .filter_map(|(name, value)| value.map(|v| (format!("${{{}}}", name), v)))
                .collect()
        } else {
            vec![]
        }
    }

    fn evaluate_arguments(&self, variables_values: &Vec<(String, String)>) -> Vec<String> {
        self.arguments
            .iter()
            .flatten()
            .cloned()
            .map(|mut argument| {
                for var_val in variables_values {
                    if argument.contains(&var_val.0) {
                        argument = argument.replace(&var_val.0, &var_val.1)
                    }
                }
                argument
            })
            .collect()
    }
}

impl Runnable for CommandEntry {
    fn run(&self, context: &mut Context) -> std::io::Result<Output> {
        let variables_values = self.variable_values(context);
        let arguments = self.evaluate_arguments(&variables_values);

        Command::new(&self.command)
            .current_dir(self.current_dir(&context.current_dir))
            .args(arguments)
            .output()
    }
}

impl Display for CommandEntry {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        if let Some(path) = &self.working_dir {
            write!(
                f,
                "{} ",
                fs::canonicalize(path)
                    .expect("Failed to canonicalize path")
                    .as_os_str()
                    .to_str()
                    .expect("failed to convert path")
            )?;
        }

        write!(f, "$ {}", &self.command)?;

        if let Some(args) = &self.arguments {
            for arg in args {
                if arg.contains(' ') {
                    write!(f, " '{}'", arg)?;
                } else {
                    write!(f, " {}", arg)?;
                }
            }
        }

        Ok(())
    }
}

#[cfg(test)]
mod tests {
    use std::env;

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
      "command": "date"
    }
  ]
}"#;

            let expected = Document {
                variables: vec![],
                entries: vec![Command(CommandEntry {
                    command: "date".to_string(),
                    working_dir: None,
                    arguments: None,
                    variables: None,
                })],
            };

            let deserialized: Document = Document::parse(json).unwrap();
            assert_eq!(expected, deserialized);
        }
    }

    mod current_dir {
        use super::*;

        #[test]
        fn return_the_give_path_when_working_dir_is_missing() {
            let command = CommandEntry {
                command: "date".to_string(),
                working_dir: None,
                arguments: None,
                variables: None,
            };

            let current_dir = current_dir();
            let result = command.current_dir(&current_dir);
            assert_eq!(current_dir, result);
        }

        #[test]
        fn return_the_constructed_path_when_working_dir_is_relative() {
            let command = CommandEntry {
                command: "date".to_string(),
                working_dir: Some(PathBuf::from("test")),
                arguments: None,
                variables: None,
            };

            let current_dir = current_dir();
            let result = command.current_dir(&current_dir);
            assert_eq!(current_dir.join("test"), result);
        }

        #[test]
        fn return_the_working_dir_when_it_is_absolute() {
            let command = CommandEntry {
                command: "date".to_string(),
                working_dir: Some(PathBuf::from("/test")),
                arguments: None,
                variables: None,
            };

            let current_dir = current_dir();
            let result = command.current_dir(&current_dir);
            assert_eq!(PathBuf::from("/test"), result);
        }
    }

    mod run {
        use std::str::from_utf8;

        use crate::domain::ContextVariable;

        use super::*;

        #[test]
        fn execute_command() {
            let command = CommandEntry {
                command: "echo".to_string(),
                working_dir: None,
                arguments: Some(vec!["Hello World!".to_string()]),
                variables: None,
            };

            let mut context = Context {
                current_dir: current_dir(),
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
        fn execute_command_that_contains_variables() {
            let command = CommandEntry {
                command: "echo".to_string(),
                working_dir: None,
                arguments: Some(vec!["Hello ${NAME}!".to_string()]),
                variables: Some(vec!["NAME".to_string()]),
            };

            let mut context = Context {
                current_dir: current_dir(),
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
    }

    fn current_dir() -> PathBuf {
        env::current_dir().expect("Failed to get the current working directory")
    }
}
