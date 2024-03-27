#![warn(missing_debug_implementations, rust_2018_idioms)]

use crate::cla::Args;
use crate::entries::Document;
use colored::Colorize;
use std::process::ExitCode;
use std::str::from_utf8;

mod cla;
mod entries;

fn main() -> ExitCode {
    let args = Args::create();

    for file in args.files() {
        let json = file.read();
        let document = Document::parse(&json).expect("Failed to parse JSON file");

        for command in document.commands() {
            println!("{}", command.to_string().bright_green());
            let output = command
                .run_from_dir(&file.parent_dir())
                .expect("The command didn't complete as expected");

            if output.status.success() {
                let stdout = from_utf8(&output.stdout).expect("Failed to read STDOUT");

                if !stdout.is_empty() {
                    println!("{}", stdout.on_green());
                }
            } else {
                println!(
                    "{} {}",
                    "Command returned error code:".red(),
                    &output
                        .status
                        .code()
                        .map_or(-1, |code| code)
                        .to_string()
                        .on_red()
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
                return ExitCode::FAILURE;
            }
        }
    }

    ExitCode::SUCCESS
}
