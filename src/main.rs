#![warn(missing_debug_implementations, rust_2018_idioms)]

use crate::entries::Document;

mod entries;

fn main() {
    let json = r#"{
"entries": [
  {
    "type": "Chapter",
    "id": "3a50daae-ab81-426f-a118-b505e7eecb49",
    "title": "Prologue"
  },
  {
    "type": "Markdown",
    "id": "483214f8-fc66-4a3a-b8dc-26401ac6a608",
    "contents": [
      "We make mistakes, and we make more mistakes, and some more, and that's how we learn."
    ]
  },
  {
    "type": "Command",
    "id": "c865e693-2d56-48d1-9c9f-57a2a42d19d8",
    "command": "date"
  }
]
}"#;

    let document = Document::parse(json).unwrap();
    println!("Document: {}", document);

    for command in document.commands() {
        command
            .run()
            .expect("The command didn't complete as expected");
    }
}
