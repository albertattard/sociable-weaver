#![warn(missing_debug_implementations, rust_2018_idioms)]

use crate::cla::Args;
use crate::entries::Document;

mod cla;
mod entries;

fn main() {
    let args = Args::create();

    for file in args.files() {
        let json = file.read();
        let document = Document::parse(&json).expect("Failed to parse JSON file");

        println!("Document: {}", document);

        for command in document.commands() {
            command
                .run_from_dir(&file.parent_dir())
                .expect("The command didn't complete as expected");
        }
    }
}
