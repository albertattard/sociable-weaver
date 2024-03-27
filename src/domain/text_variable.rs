use serde::Deserialize;

#[derive(Debug, PartialEq, Deserialize)]
pub(crate) struct TextVariable {
    name: String,
}
