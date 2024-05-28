use std::fmt::{Display, Formatter};

use serde::Deserialize;

#[derive(Debug, PartialEq, Deserialize)]
pub(crate) struct TodoEntry {
    comments: Option<Vec<String>>,
}

impl TodoEntry {}

impl Display for TodoEntry {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        writeln!(f, "TODO")?;
        if let Some(comments) = &self.comments {
            for comment in comments {
                writeln!(f, " {}", comment)?;
            }
        }
        Ok(())
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    mod deserialize_tests {
        use crate::domain::Document;
        use crate::domain::Entry::Todo;

        use super::*;

        #[test]
        fn return_deserialized_breakpoint_when_given_minimum_options() {
            let json = r#"{
  "entries": [
    {
      "type": "Todo"
    }
  ]
}"#;

            let expected = Document {
                entries: vec![Todo(TodoEntry { comments: None })],
            };

            let deserialized: Document = Document::parse(json).unwrap();
            assert_eq!(expected, deserialized);
        }

        #[test]
        fn return_deserialized_breakpoint_when_given_all_options() {
            let json = r#"{
  "entries": [
    {
      "type": "Todo",
      "comments": ["Testing todo"]
    }
  ]
}"#;

            let expected = Document {
                entries: vec![Todo(TodoEntry {
                    comments: Some(vec!["Testing todo".to_string()]),
                })],
            };

            let deserialized: Document = Document::parse(json).unwrap();
            assert_eq!(expected, deserialized);
        }
    }
}
