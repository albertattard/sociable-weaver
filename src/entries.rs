use std::fmt::{Debug, Display, Formatter};

use serde::Deserialize;

#[derive(Debug, PartialEq, Deserialize)]
pub(crate) struct Document {
    entries: Vec<Entry>,
}

impl Document {
    /* TODO: Don't leak the serde result */
    pub(crate) fn parse(json: &str) -> serde_json::Result<Self> {
        serde_json::from_str(json)
    }
}

impl Display for Document {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        f.debug_struct("Entries")
            .field("entries", &self.entries)
            .finish()
    }
}

/// Reference: https://serde.rs/enum-representations.html
#[derive(Debug, PartialEq, Deserialize)]
#[serde(tag = "type")]
pub(crate) enum Entry {
    Chapter(ChapterEntry),
    Markdown(MarkdownEntry),
}

#[derive(Debug, PartialEq, Deserialize)]
pub(crate) struct ChapterEntry {
    id: String,
    title: String,
}

#[derive(Debug, PartialEq, Deserialize)]
pub(crate) struct MarkdownEntry {
    id: String,
    contents: Vec<String>,
}

#[cfg(test)]
mod tests {
    use crate::entries::Entry::{Chapter, Markdown};

    use super::*;

    #[test]
    fn test_deserialize_chapter_variants() {
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

    #[test]
    fn test_deserialize_markdown_variants() {
        let json = r#"{
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
            entries: vec![Markdown(MarkdownEntry { id: "483214f8-fc66-4a3a-b8dc-26401ac6a608".to_string(), contents: vec!["We make mistakes, and we make more mistakes, and some more, and that's how we learn.".to_string()] })]
        };

        let deserialized: Document = Document::parse(json).unwrap();
        assert_eq!(expected, deserialized);
    }
}
