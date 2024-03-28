use serde::Deserialize;

#[derive(Debug, PartialEq, Deserialize)]
pub(crate) enum HeadingLevel {
    H1,
    H2,
    H3,
    H4,
}

#[derive(Debug, PartialEq, Deserialize)]
pub(crate) struct HeadingEntry {
    level: HeadingLevel,
    title: String,
}

#[cfg(test)]
mod tests {
    use crate::domain::heading::HeadingLevel::H1;
    use crate::domain::Entry::Heading;
    use crate::Document;

    use super::*;

    #[test]
    fn return_deserialized_heading() {
        let json = r#"{
  "variables": [],
  "entries": [
    {
      "type": "Heading",
      "level": "H1",
      "title": "Prologue"
    }
  ]
}"#;

        let expected = Document {
            variables: vec![],
            entries: vec![Heading(HeadingEntry {
                level: H1,
                title: "Prologue".to_string(),
            })],
        };

        let deserialized: Document = Document::parse(json).unwrap();
        assert_eq!(expected, deserialized);
    }
}
