use std::fmt::{Display, Formatter};
use std::fs::read_to_string;
use std::path::PathBuf;

use clap::Parser;

use crate::utils::paths;

/// A simple application that builds documentation and tests code.
#[derive(Parser, Debug)]
#[command(author, version, about, long_about = None)]
pub(crate) struct Args {
    /// Name of the JSON file to parse
    #[arg(short, long, default_value = "sw-runbook.json")]
    file_name: String,
}

impl Args {
    pub(crate) fn create() -> Self {
        Args::parse()
    }

    pub(crate) fn files(&self) -> Vec<DocumentFile> {
        vec![DocumentFile::new(self.file_path())]
    }

    fn file_path(&self) -> PathBuf {
        PathBuf::from(&self.file_name)
    }
}

pub(crate) struct DocumentFile {
    path: PathBuf,
}

impl DocumentFile {
    fn new(path: PathBuf) -> Self {
        DocumentFile { path }
    }

    pub(crate) fn parent_dir(&self) -> PathBuf {
        paths::parent_dir(&self.path)
    }

    pub(crate) fn read(&self) -> String {
        read_to_string(&self.path).unwrap_or_else(|_| {
            panic!(
                "Failed to read JSON file: {}",
                paths::path_as_str(&self.path)
            )
        })
    }
}

impl Display for DocumentFile {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}", paths::path_as_str(&self.path))
    }
}
