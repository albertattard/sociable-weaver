use std::fmt::{Display, Formatter};
use std::fs::read_to_string;
use std::path::PathBuf;
use std::{env, fs};

use clap::Parser;

/// A simple application that builds documentation and tests code.
#[derive(Parser, Debug)]
#[command(author, version, about, long_about = None)]
pub(crate) struct Args {
    /// Name of the JSON file to parse
    #[arg(short, long, default_value = "book.json")]
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
        fs::canonicalize(&self.path)
            .expect("Failed to canonicalize path")
            .parent()
            .map(|p| p.to_path_buf())
            .unwrap_or_else(|| {
                env::current_dir().expect("Failed to get the current working directory")
            })
    }

    pub(crate) fn read(&self) -> String {
        read_to_string(&self.path)
            .unwrap_or_else(|_| panic!("Failed to read JSON file: {}", self.path_as_str()))
    }

    fn path_as_str(&self) -> String {
        fs::canonicalize(&self.path)
            .expect("Failed to canonicalize path")
            .as_os_str()
            .to_str()
            .expect("failed to convert path")
            .to_string()
    }
}

impl Display for DocumentFile {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}", self.path_as_str())
    }
}
