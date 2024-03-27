use std::fmt::{Display, Formatter};
use std::fs;
use std::path::{Path, PathBuf};
use std::process::{Command, Output};

use serde::Deserialize;

#[derive(Debug, PartialEq, Deserialize)]
pub(crate) struct CommandEntry {
    id: String,
    command: String,
    working_dir: Option<PathBuf>,
    arguments: Option<Vec<String>>,
}

impl CommandEntry {
    pub(crate) fn run_from_dir<P: AsRef<Path>>(&self, current_dir: &P) -> std::io::Result<Output> {
        Command::new(&self.command)
            .current_dir(self.current_dir(current_dir))
            .args(self.arguments.as_ref().unwrap_or(&vec![]))
            .output()
    }

    fn current_dir<P: AsRef<Path>>(&self, current_dir: &P) -> PathBuf {
        self.working_dir.as_ref().map_or_else(
            || current_dir.as_ref().to_path_buf(),
            |path| current_dir.as_ref().join(path),
        )
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
        use crate::entries::Document;
        use crate::entries::Entry::Command;

        use super::*;

        #[test]
        fn return_deserialized_structure_when_given_minimum_options() {
            let json = r#"{
"entries": [
  {
    "type": "Command",
    "id": "c865e693-2d56-48d1-9c9f-57a2a42d19d8",
    "command": "date"
  }
]
}"#;

            let expected = Document {
                entries: vec![Command(CommandEntry {
                    id: "c865e693-2d56-48d1-9c9f-57a2a42d19d8".to_string(),
                    command: "date".to_string(),
                    working_dir: None,
                    arguments: None,
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
                id: "c865e693-2d56-48d1-9c9f-57a2a42d19d8".to_string(),
                command: "date".to_string(),
                working_dir: None,
                arguments: None,
            };

            let current_dir = current_dir();
            let result = command.current_dir(&current_dir);
            assert_eq!(current_dir, result);
        }

        #[test]
        fn return_the_constructed_path_when_working_dir_is_relative() {
            let command = CommandEntry {
                id: "c865e693-2d56-48d1-9c9f-57a2a42d19d8".to_string(),
                command: "date".to_string(),
                working_dir: Some(PathBuf::from("test")),
                arguments: None,
            };

            let current_dir = current_dir();
            let result = command.current_dir(&current_dir);
            assert_eq!(current_dir.join("test"), result);
        }

        #[test]
        fn return_the_working_dir_when_it_is_absolute() {
            let command = CommandEntry {
                id: "c865e693-2d56-48d1-9c9f-57a2a42d19d8".to_string(),
                command: "date".to_string(),
                working_dir: Some(PathBuf::from("/test")),
                arguments: None,
            };

            let current_dir = current_dir();
            let result = command.current_dir(&current_dir);
            assert_eq!(PathBuf::from("/test"), result);
        }
    }

    mod run {
        use std::str::from_utf8;

        use super::*;

        #[test]
        fn execute_command_and_return_result() {
            let command = CommandEntry {
                id: "c865e693-2d56-48d1-9c9f-57a2a42d19d8".to_string(),
                command: "date".to_string(),
                working_dir: None,
                arguments: None,
            };

            let result = command.run_from_dir(&current_dir());
            assert!(result.is_ok());

            let output = result.unwrap();
            assert!(output.status.success());
            let date = from_utf8(&output.stdout)
                .expect("Failed to parse stdout as UTF-8")
                .trim();
            assert!(!date.is_empty());
        }
    }

    fn current_dir() -> PathBuf {
        env::current_dir().expect("Failed to get the current working directory")
    }
}
