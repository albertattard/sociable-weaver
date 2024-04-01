use std::env;
use std::fmt::{Debug, Display, Formatter};
use std::path::PathBuf;
use std::process::Output;
use std::str::from_utf8;

use crate::domain::breakpoint::BreakpointEntry;
use colored::Colorize;
use serde::Deserialize;

use crate::domain::command::CommandEntry;
use crate::domain::heading::HeadingEntry;
use crate::domain::markdown::MarkdownEntry;
use crate::domain::text_variable::TextVariable;
use crate::domain::todo::TodoEntry;
use crate::domain::Variable::Text;

mod breakpoint;
pub(crate) mod command;
pub(crate) mod heading;
pub(crate) mod markdown;
pub(crate) mod text_variable;
pub(crate) mod todo;

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

    pub(crate) fn entries(&self) -> Vec<&Entry> {
        self.entries.iter().collect()
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
    Heading(HeadingEntry),
    Markdown(MarkdownEntry),
    Command(CommandEntry),
    Breakpoint(BreakpointEntry),
    Todo(TodoEntry),
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
}

pub(crate) struct Executor {}

impl Executor {
    pub(crate) fn execute(runnable: &dyn Runnable, context: &mut Context) -> Result<(), String> {
        println!("{}", runnable.to_string().truecolor(100, 100, 100));
        let output = runnable
            .run(context)
            .expect("The command(s) didn't complete as expected");

        if output.status.success() {
            Self::print_success(&output)
        } else {
            Self::print_failure(&output)
        }
    }

    fn print_success(output: &Output) -> Result<(), String> {
        let stdout = from_utf8(&output.stdout).expect("Failed to read STDOUT");

        if !stdout.is_empty() {
            println!("{}", stdout.bright_green());
        }

        Ok(())
    }

    fn print_failure(output: &Output) -> Result<(), String> {
        let x = &output.status.code().map_or(-1, |code| code);
        println!(
            "{} {}",
            "Command(s) returned error code:".red(),
            x.to_string().red()
        );
        println!(
            "{}",
            from_utf8(&output.stdout)
                .expect("Failed to read STDOUT")
                .red()
        );
        println!(
            "{}",
            from_utf8(&output.stderr)
                .expect("Failed to read STDERR")
                .red()
        );

        Err(format!("Command(s) returned error code: {}", x))
    }
}
