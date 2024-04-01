#![warn(missing_debug_implementations, rust_2018_idioms)]

use std::io;
use std::process::ExitCode;

use crate::domain::Entry::{Breakpoint, Command};
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

        for entry in document.entries() {
            match entry {
                Command(command) => {
                    if Executor::execute(command, &mut context).is_err() {
                        return ExitCode::FAILURE;
                    }
                }
                Breakpoint => {
                    println!("Press enter to continue");
                    let mut input = String::new();
                    if io::stdin().read_line(&mut input).is_err() {
                        return ExitCode::FAILURE;
                    }
                }
                _ => {}
            }
        }
    }

    ExitCode::SUCCESS
}
