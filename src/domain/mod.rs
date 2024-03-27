use std::fmt::{Debug, Display, Formatter};

use serde::Deserialize;

use crate::domain::command::CommandEntry;
use crate::domain::header::HeaderEntry;
use crate::domain::markdown::MarkdownEntry;
use crate::domain::text_variable::TextVariable;

pub(crate) mod command;
pub(crate) mod header;
pub(crate) mod markdown;
pub(crate) mod text_variable;

#[derive(Debug, PartialEq, Deserialize)]
pub(crate) struct Document {
    variables: Vec<Variable>,
    entries: Vec<Entry>,
}

impl Document {
    /* TODO: Don't leak the serde result */
    pub(crate) fn parse(json: &str) -> serde_json::Result<Self> {
        serde_json::from_str(json)
    }

    pub(crate) fn commands(&self) -> Vec<&CommandEntry> {
        self.entries
            .iter()
            .filter_map(|entry| match entry {
                Entry::Command(e) => Some(e),
                _ => None,
            })
            .collect()
    }
}

impl Display for Document {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        f.debug_struct("Document")
            .field("variables", &self.variables)
            .field("entries", &self.entries)
            .finish()
    }
}

/// Reference: https://serde.rs/enum-representations.html
#[derive(Debug, PartialEq, Deserialize)]
#[serde(tag = "type")]
pub(crate) enum Entry {
    Header(HeaderEntry),
    Markdown(MarkdownEntry),
    Command(CommandEntry),
}

#[derive(Debug, PartialEq, Deserialize)]
#[serde(tag = "type")]
pub(crate) enum Variable {
    Text(TextVariable),
}
