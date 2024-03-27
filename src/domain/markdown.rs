use serde::Deserialize;

#[derive(Debug, PartialEq, Deserialize)]
pub(crate) struct MarkdownEntry {
    id: String,
    contents: Vec<String>,
}

#[cfg(test)]
mod tests {
    use crate::Document;
    use crate::domain::Entry::Markdown;

    use super::*;

    #[test]
    fn return_deserialized_structure() {
        let json = r#"{
  "variables": [],
  "entries": [
    {
      "type": "Markdown",
      "id": "483214f8-fc66-4a3a-b8dc-26401ac6a608",
      "contents": [
        "We make mistakes, and we make more mistakes, and some more, and that's how we learn."
      ]
    }
  ]
}"#;

        let expected = Document {
            variables: vec![],
            entries: vec![Markdown(MarkdownEntry { id: "483214f8-fc66-4a3a-b8dc-26401ac6a608".to_string(), contents: vec!["We make mistakes, and we make more mistakes, and some more, and that's how we learn.".to_string()] })]
        };

        let deserialized: Document = Document::parse(json).unwrap();
        assert_eq!(expected, deserialized);
    }
}
