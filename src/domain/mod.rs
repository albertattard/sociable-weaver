use std::env;
use std::fmt::{Debug, Display, Formatter};
use std::path::PathBuf;
use std::process::Output;
use std::str::from_utf8;

use colored::Colorize;
use serde::Deserialize;

use crate::domain::command::CommandEntry;
use crate::domain::header::HeaderEntry;
use crate::domain::markdown::MarkdownEntry;
use crate::domain::text_variable::TextVariable;
use crate::domain::Variable::Text;

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

    pub(crate) fn runnables(&self) -> Vec<&impl Runnable> {
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

#[derive(Debug, PartialEq)]
pub(crate) struct ContextVariable {
    name: String,
    value: String,
}

impl From<&Variable> for ContextVariable {
    fn from(value: &Variable) -> Self {
        match value {
            Text(variable) => ContextVariable::from(variable),
        }
    }
}

impl From<&TextVariable> for ContextVariable {
    fn from(value: &TextVariable) -> Self {
        ContextVariable {
            name: value.name(),
            value: value.value(),
        }
    }
}

#[derive(Debug, PartialEq)]
pub(crate) struct Context {
    current_dir: PathBuf,
    variables: Vec<ContextVariable>,
}

impl Context {
    pub(crate) fn value(&self, name: &str) -> Option<String> {
        self.variables
            .iter()
            .find(|v| v.name == name)
            .map(|v| v.value.clone())
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
    fn from(document: &Document) -> Context {
        Context {
            current_dir: Context::current_dir(),
            variables: document
                .variables
                .iter()
                .map(ContextVariable::from)
                .collect(),
        }
    }
}

pub(crate) trait Runnable: ToString {
    fn run(&self, context: &mut Context) -> std::io::Result<Output>;

    fn execute(&self, context: &mut Context) -> Result<(), String> {
        println!("{}", self.to_string().bright_green());
        let output = self
            .run(context)
            .expect("The command didn't complete as expected");

        if output.status.success() {
            Self::on_success(&output)
        } else {
            Self::on_failure(&output)
        }
    }

    fn on_success(output: &Output) -> Result<(), String> {
        let stdout = from_utf8(&output.stdout).expect("Failed to read STDOUT");

        if !stdout.is_empty() {
            println!("{}", stdout.on_green());
        }

        Ok(())
    }

    fn on_failure(output: &Output) -> Result<(), String> {
        let x = &output.status.code().map_or(-1, |code| code);
        println!(
            "{} {}",
            "Command returned error code:".red(),
            x.to_string().on_red()
        );
        println!(
            "{}",
            from_utf8(&output.stdout)
                .expect("Failed to read STDOUT")
                .on_red()
        );
        println!(
            "{}",
            from_utf8(&output.stderr)
                .expect("Failed to read STDERR")
                .red()
        );

        Err(format!("Command returned error code: {}", x))
    }
}
