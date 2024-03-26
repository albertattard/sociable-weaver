use serde::Deserialize;

#[derive(Debug, PartialEq, Deserialize)]
pub(crate) struct ChapterEntry {
    id: String,
    title: String,
}

#[cfg(test)]
mod tests {
    use crate::entries::Entry::Chapter;
    use crate::Document;

    use super::*;

    #[test]
    fn test_deserialize() {
        let json = r#"{
"entries": [
  {
    "type": "Chapter",
    "id": "3a50daae-ab81-426f-a118-b505e7eecb49",
    "title": "Prologue"
  }
]
}"#;

        let expected = Document {
            entries: vec![Chapter(ChapterEntry {
                id: "3a50daae-ab81-426f-a118-b505e7eecb49".to_string(),
                title: "Prologue".to_string(),
            })],
        };

        let deserialized: Document = Document::parse(json).unwrap();
        assert_eq!(expected, deserialized);
    }
}
