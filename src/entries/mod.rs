use std::fmt::{Debug, Display, Formatter};

use serde::Deserialize;

use crate::entries::chapter::ChapterEntry;
use crate::entries::command::CommandEntry;
use crate::entries::markdown::MarkdownEntry;

pub(crate) mod chapter;
pub(crate) mod command;
pub(crate) mod markdown;

#[derive(Debug, PartialEq, Deserialize)]
pub(crate) struct Document {
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
    Command(CommandEntry),
}
