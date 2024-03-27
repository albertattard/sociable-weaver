use std::path::{Path, PathBuf};
use std::{env, fs};

pub(crate) fn parent_dir<P: AsRef<Path>>(path: P) -> PathBuf {
    fs::canonicalize(path)
        .expect("Failed to canonicalize path")
        .parent()
        .map(|p| p.to_path_buf())
        .unwrap_or_else(current_dir)
}

pub(crate) fn path_as_str<P: AsRef<Path>>(path: P) -> String {
    fs::canonicalize(path)
        .expect("Failed to canonicalize path")
        .as_os_str()
        .to_str()
        .expect("failed to convert path")
        .to_string()
}

pub(crate) fn current_dir() -> PathBuf {
    env::current_dir().expect("Failed to get the current working directory")
}
