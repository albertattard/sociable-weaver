use serde::Deserialize;

#[derive(Debug, PartialEq, Deserialize)]
pub(crate) enum HeaderLevel {
    H1,
    H2,
    H3,
    H4,
}

#[derive(Debug, PartialEq, Deserialize)]
pub(crate) struct HeaderEntry {
    level: HeaderLevel,
    title: String,
}

#[cfg(test)]
mod tests {
    use crate::domain::header::HeaderLevel::H1;
    use crate::domain::Entry::Header;
    use crate::Document;

    use super::*;

    #[test]
    fn return_deserialized_structure() {
        let json = r#"{
  "variables": [],
  "entries": [
    {
      "type": "Header",
      "level": "H1",
      "title": "Prologue"
    }
  ]
}"#;

        let expected = Document {
            variables: vec![],
            entries: vec![Header(HeaderEntry {
                level: H1,
                title: "Prologue".to_string(),
            })],
        };

        let deserialized: Document = Document::parse(json).unwrap();
        assert_eq!(expected, deserialized);
    }
}
