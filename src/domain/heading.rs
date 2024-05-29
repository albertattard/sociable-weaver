use serde::Deserialize;

use crate::domain::MarkdownRunnable;

#[derive(Debug, PartialEq, Deserialize)]
pub(crate) enum HeadingLevel {
    H1,
    H2,
    H3,
    H4,
    H5,
}

#[derive(Debug, PartialEq, Deserialize)]
pub(crate) struct HeadingEntry {
    level: HeadingLevel,
    title: String,
}

impl MarkdownRunnable for HeadingEntry {
    fn run_markdown(&self) -> Result<String, String> {
        let prefix = match self.level {
            HeadingLevel::H1 => "#",
            HeadingLevel::H2 => "##",
            HeadingLevel::H3 => "###",
            HeadingLevel::H4 => "####",
            HeadingLevel::H5 => "#####",
        };

        Ok(format!("{} {}\n", prefix, self.title))
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    mod deserialize_tests {
        use crate::domain::heading::HeadingLevel::H1;
        use crate::domain::Entry::Heading;
        use crate::Document;

        use super::*;

        #[test]
        fn return_deserialized_heading() {
            let json = r#"{
  "entries": [
    {
      "type": "Heading",
      "level": "H1",
      "title": "Prologue"
    }
  ]
}"#;

            let expected = Document {
                entries: vec![Heading(HeadingEntry {
                    level: H1,
                    title: "Prologue".to_string(),
                })],
            };

            let deserialized: Document = Document::parse(json).unwrap();
            assert_eq!(expected, deserialized);
        }
    }

    mod markdown_runnable_tests {
        use crate::domain::heading::HeadingLevel::{H1, H2, H3, H4, H5};

        use super::*;

        #[test]
        fn format_h1() {
            /* Given */
            let h1 = HeadingEntry {
                level: H1,
                title: "Heading Level 1".to_string(),
            };

            /* When */
            let md = h1.run_markdown();

            /* Then */
            assert_eq!(Ok("# Heading Level 1\n".to_string()), md);
        }

        #[test]
        fn format_h2() {
            /* Given */
            let h2 = HeadingEntry {
                level: H2,
                title: "Heading Level 2".to_string(),
            };

            /* When */
            let md = h2.run_markdown();

            /* Then */
            assert_eq!(Ok("## Heading Level 2\n".to_string()), md);
        }

        #[test]
        fn format_h3() {
            /* Given */
            let h3 = HeadingEntry {
                level: H3,
                title: "Heading Level 3".to_string(),
            };

            /* When */
            let md = h3.run_markdown();

            /* Then */
            assert_eq!(Ok("### Heading Level 3\n".to_string()), md);
        }

        #[test]
        fn format_h4() {
            /* Given */
            let h4 = HeadingEntry {
                level: H4,
                title: "Heading Level 4".to_string(),
            };

            /* When */
            let md = h4.run_markdown();

            /* Then */
            assert_eq!(Ok("#### Heading Level 4\n".to_string()), md);
        }

        #[test]
        fn format_h5() {
            /* Given */
            let h5 = HeadingEntry {
                level: H5,
                title: "Heading Level 5".to_string(),
            };

            /* When */
            let md = h5.run_markdown();

            /* Then */
            assert_eq!(Ok("##### Heading Level 5\n".to_string()), md);
        }
    }
}
