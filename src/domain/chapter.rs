use serde::Deserialize;

#[derive(Debug, PartialEq, Deserialize)]
pub(crate) struct ChapterEntry {
    id: String,
    title: String,
}

#[cfg(test)]
mod tests {
    use crate::Document;
    use crate::domain::Entry::Chapter;

    use super::*;

    #[test]
    fn return_deserialized_structure() {
        let json = r#"{
  "variables": [],
  "entries": [
    {
      "type": "Chapter",
      "id": "3a50daae-ab81-426f-a118-b505e7eecb49",
      "title": "Prologue"
    }
  ]
}"#;

        let expected = Document {
            variables: vec![],
            entries: vec![Chapter(ChapterEntry {
                id: "3a50daae-ab81-426f-a118-b505e7eecb49".to_string(),
                title: "Prologue".to_string(),
            })],
        };

        let deserialized: Document = Document::parse(json).unwrap();
        assert_eq!(expected, deserialized);
    }
}
