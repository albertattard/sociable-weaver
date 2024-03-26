use std::path::PathBuf;

use serde::Deserialize;

#[derive(Debug, PartialEq, Deserialize)]
pub(crate) struct CommandEntry {
    id: String,
    command: String,
    working_dir: Option<PathBuf>,
    arguments: Option<Vec<String>>,
}

#[cfg(test)]
mod tests {
    use crate::entries::Document;
    use crate::entries::Entry::Command;

    use super::*;

    #[test]
    fn test_deserialize_with_minimum_options() {
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
                working_dir: None,
                command: "date".to_string(),
                arguments: None,
            })],
        };

        let deserialized: Document = Document::parse(json).unwrap();
        assert_eq!(expected, deserialized);
    }
}
