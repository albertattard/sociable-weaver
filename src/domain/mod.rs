use std::env;
use std::fmt::{Debug, Display, Formatter};
use std::path::PathBuf;
use std::process::Output;
use std::str::from_utf8;

use colored::Colorize;
use serde::Deserialize;

use crate::domain::breakpoint::BreakpointEntry;
use crate::domain::command::CommandEntry;
use crate::domain::display_file::DisplayFileEntry;
use crate::domain::heading::HeadingEntry;
use crate::domain::markdown::MarkdownEntry;
use crate::domain::todo::TodoEntry;
use crate::utils::paths::current_dir;

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

#[derive(Debug, PartialEq)]
pub(crate) struct Context {
    current_dir: PathBuf,
}

impl Context {
    pub(crate) fn empty() -> Self {
        Context {
            current_dir: current_dir(),
        }
    }

    pub(crate) fn with_current_dir(mut self, current_dir: PathBuf) -> Self {
        self.current_dir = current_dir;
        self
    }

    fn current_dir() -> PathBuf {
        env::current_dir().expect("Failed to get the current working directory")
    }
}

impl From<&Document> for Context {
    fn from(_document: &Document) -> Context {
        Context {
            current_dir: Context::current_dir(),
        }
    }
}

pub(crate) trait MarkdownRunnable {
    fn to_markdown(&self, context: &mut Context) -> Result<String, String>;
}
