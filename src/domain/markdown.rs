use serde::Deserialize;

#[derive(Debug, PartialEq, Deserialize)]
pub(crate) struct MarkdownEntry {
    contents: Vec<String>,
    tags: Option<Vec<String>>,
}

#[cfg(test)]
mod tests {
    use crate::domain::Entry::Markdown;
    use crate::Document;

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
