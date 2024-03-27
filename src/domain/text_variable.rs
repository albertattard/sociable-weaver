use std::fmt::{Display, Formatter};

use serde::Deserialize;

#[derive(Debug, PartialEq, Deserialize)]
pub(crate) struct TextVariable {
    name: String,
    default_value: Option<String>,
}

impl TextVariable {
    pub(crate) fn name(&self) -> String {
        self.name.clone()
    }

    pub(crate) fn value(&self) -> String {
        /* TODO: We are starting with the default value for now, because that will suffice. We will
            then add more sophisticated implementation that reads variables from STDIN or files. */
        self.default_value
            .as_ref()
            .expect("Only default values are currently supported")
            .clone()
    }
}

impl Display for TextVariable {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}", self.name)?;

        if let Some(default_value) = &self.default_value {
            write!(f, " ({})", default_value)?;
        }

        Ok(())
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    mod deserialized {
        use crate::domain::Document;
        use crate::domain::Variable::Text;

        use super::*;

        #[test]
        fn return_deserialized_text_variable_when_given_minimum_options() {
            let json = r#"{
  "variables": [
    {
      "type": "Text",
      "name": "VAR_NAME"
    }
  ],
  "entries": []
}"#;

            let expected = Document {
                variables: vec![Text(TextVariable {
                    name: "VAR_NAME".to_string(),
                    default_value: None,
                })],
                entries: vec![],
            };

            let deserialized: Document = Document::parse(json).unwrap();
            assert_eq!(expected, deserialized);
        }

        #[test]
        fn return_deserialized_text_variable_when_given_all_options() {
            let json = r#"{
  "variables": [
    {
      "type": "Text",
      "name": "VAR_NAME",
      "default_value": "DEFAULT-VALUE"
    }
  ],
  "entries": []
}"#;

            let expected = Document {
                variables: vec![Text(TextVariable {
                    name: "VAR_NAME".to_string(),
                    default_value: Some("DEFAULT-VALUE".to_string()),
                })],
                entries: vec![],
            };

            let deserialized: Document = Document::parse(json).unwrap();
            assert_eq!(expected, deserialized);
        }
    }

    mod value {
        use crate::domain::text_variable::TextVariable;

        #[test]
        fn return_the_default_value_when_one_is_not_provided() {
            let default_value = "TEST_VALUE".to_string();
            let variable = TextVariable {
                name: "TEST_VARIABLE".to_string(),
                default_value: Some(default_value.clone()),
            };
            let value = variable.value();
            assert_eq!(default_value, value);
        }
    }
}
