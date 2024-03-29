#![warn(missing_debug_implementations, rust_2018_idioms)]

use std::process::ExitCode;

use crate::domain::Executor;
use crate::domain::{Context, Document};
use crate::utils::cla::Args;

mod domain;
mod utils;

fn main() -> ExitCode {
    let args = Args::create();

    for file in args.files() {
        println!("Running file: {}", file);
        let json = file.read();
        let document = Document::parse(&json).expect("Failed to parse JSON file");
        let mut context = Context::from(&document).with_current_dir(file.parent_dir());

        for command in document.runnables() {
            if Executor::execute(command, &mut context).is_err() {
                return ExitCode::FAILURE;
            }
        }
    }

    ExitCode::SUCCESS
}
