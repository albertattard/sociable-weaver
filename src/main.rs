#![warn(missing_debug_implementations, rust_2018_idioms)]

use std::process::ExitCode;

use crate::cla::Args;
use crate::domain::Runnable;
use crate::domain::{Context, Document};

mod cla;
mod domain;

fn main() -> ExitCode {
    let args = Args::create();

    for file in args.files() {
        println!("Running file: {}", file);
        let json = file.read();
        let document = Document::parse(&json).expect("Failed to parse JSON file");
        let mut context = Context::from(&document);

        for command in document.runnables() {
            if command.execute(&mut context).is_err() {
                return ExitCode::FAILURE;
            }
        }
    }

    ExitCode::SUCCESS
}
