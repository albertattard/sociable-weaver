use std::fmt::{Debug, Display, Formatter};

use serde::Deserialize;

use crate::domain::breakpoint::BreakpointEntry;
use crate::domain::command::CommandEntry;
use crate::domain::display_file::DisplayFileEntry;
use crate::domain::heading::HeadingEntry;
use crate::domain::markdown::MarkdownEntry;
use crate::domain::todo::TodoEntry;

pub(crate) mod breakpoint;
pub(crate) mod command;
pub(crate) mod display_file;
pub(crate) mod heading;
pub(crate) mod markdown;
pub(crate) mod todo;

#[derive(Debug, PartialEq, Deserialize)]
pub(crate) struct Document {
    entries: Vec<Entry>,
}

impl Document {
    pub(crate) fn new(entries: Vec<Entry>) -> Self {
        return Document { entries };
    }

    /* TODO: Don't leak the serde result */
    pub(crate) fn parse(json: &str) -> serde_json::Result<Self> {
        serde_json::from_str(json)
    }

    pub(crate) fn entries(&self) -> Vec<&Entry> {
        self.entries.iter().collect()
    }
}

impl Display for Document {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        f.debug_struct("Document")
            .field("entries", &self.entries)
            .finish()
    }
}

/// Reference: https://serde.rs/enum-representations.html
#[derive(Debug, PartialEq, Deserialize)]
#[serde(tag = "type")]
pub(crate) enum Entry {
    Breakpoint(BreakpointEntry),
    Command(CommandEntry),
    DisplayFile(DisplayFileEntry),
    Heading(HeadingEntry),
    Markdown(MarkdownEntry),
    Todo(TodoEntry),
}

pub(crate) trait MarkdownRunnable {
    fn run_markdown(&self) -> Result<String, String>;
}
