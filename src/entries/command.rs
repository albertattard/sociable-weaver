use std::path::PathBuf;
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
    pub(crate) fn run(&self) -> std::io::Result<Output> {
        Command::new(&self.command)
            .args(self.arguments.as_ref().unwrap_or(&vec![]))
            .output()
    }
}

#[cfg(test)]
mod tests {
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

            let result = command.run();
            assert!(result.is_ok());

            let output = result.unwrap();
            assert!(output.status.success());
            let date = from_utf8(&output.stdout)
                .expect("Failed to parse stdout as UTF-8")
                .trim();
            assert!(!date.is_empty());
        }
    }
}
