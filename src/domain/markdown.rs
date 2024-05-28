use serde::Deserialize;

use crate::domain::{Context, MarkdownRunnable};

#[derive(Debug, PartialEq, Deserialize)]
pub(crate) struct MarkdownEntry {
    contents: Vec<String>,
    tags: Option<Vec<String>>,
}

impl MarkdownRunnable for MarkdownEntry {
    fn to_markdown(&self, context: &mut Context) -> Result<String, String> {
        Ok(format!("{}\n", self.contents.join("\n")))
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    mod deserialize_tests {
        use crate::domain::markdown::MarkdownEntry;
        use crate::domain::Document;
        use crate::domain::Entry::Markdown;

        use super::*;

        #[test]
        fn return_deserialized_markdown_when_given_minimum_options() {
            let json = r#"{
  "variables": [],
  "entries": [
    {
      "type": "Markdown",
      "contents": [
        "We make mistakes, and we make more mistakes, and some more, and that's how we learn."
      ]
    }
  ]
}"#;

            let expected = Document {
                variables: vec![],
                entries: vec![Markdown(MarkdownEntry {
                    contents: vec!["We make mistakes, and we make more mistakes, and some more, and that's how we learn.".to_string()],
                    tags: None,
                })],
            };

            let deserialized: Document = Document::parse(json).unwrap();
            assert_eq!(expected, deserialized);
        }

        #[test]
        fn return_deserialized_markdown_when_given_all_options() {
            let json = r#"{
  "variables": [],
  "entries": [
    {
      "type": "Markdown",
      "contents": [
        "We make mistakes, and we make more mistakes, and some more, and that's how we learn."
      ],
      "tags": [
        "test"
      ]
    }
  ]
}"#;

            let expected = Document {
                variables: vec![],
                entries: vec![Markdown(MarkdownEntry {
                    contents: vec!["We make mistakes, and we make more mistakes, and some more, and that's how we learn.".to_string()],
                    tags: Some(vec!["test".to_string()]),
                })],
            };

            let deserialized: Document = Document::parse(json).unwrap();
            assert_eq!(expected, deserialized);
        }
    }
}
