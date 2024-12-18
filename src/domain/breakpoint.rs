use std::fmt::{Display, Formatter};

use serde::Deserialize;

#[derive(Debug, PartialEq, Deserialize)]
pub(crate) struct BreakpointEntry {
    comment: Option<String>,
}

impl BreakpointEntry {}

impl Display for BreakpointEntry {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "Breakpoint")?;
        if let Some(comment) = &self.comment {
            write!(f, " ({})", comment)?;
        }
        writeln!(f)
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    mod deserialized {
        use crate::domain::Document;
        use crate::domain::Entry::Breakpoint;

        use super::*;

        #[test]
        fn return_deserialized_breakpoint_when_given_minimum_options() {
            let json = r#"{
  "entries": [
    {
      "type": "Breakpoint"
    }
  ]
}"#;

            let expected = Document::new(vec![Breakpoint(BreakpointEntry { comment: None })]);

            let deserialized: Document = Document::parse(json).unwrap();
            assert_eq!(expected, deserialized);
        }

        #[test]
        fn return_deserialized_breakpoint_when_given_all_options() {
            let json = r#"{
  "entries": [
    {
      "type": "Breakpoint",
      "comment": "Testing breakpoints"
    }
  ]
}"#;

            let expected = Document::new(vec![Breakpoint(BreakpointEntry {
                comment: Some("Testing breakpoints".to_string()),
            })]);

            let deserialized: Document = Document::parse(json).unwrap();
            assert_eq!(expected, deserialized);
        }
    }
}
