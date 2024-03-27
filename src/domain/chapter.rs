use serde::Deserialize;

#[derive(Debug, PartialEq, Deserialize)]
pub(crate) struct ChapterEntry {
    title: String,
}

#[cfg(test)]
mod tests {
    use crate::domain::Entry::Chapter;
    use crate::Document;

    use super::*;

    #[test]
    fn return_deserialized_structure() {
        let json = r#"{
  "variables": [],
  "entries": [
    {
      "type": "Chapter",
      "title": "Prologue"
    }
  ]
}"#;

        let expected = Document {
            variables: vec![],
            entries: vec![Chapter(ChapterEntry {
                title: "Prologue".to_string(),
            })],
        };

        let deserialized: Document = Document::parse(json).unwrap();
        assert_eq!(expected, deserialized);
    }
}
