use std::fs;
use std::path::PathBuf;

use serde::Deserialize;

use crate::domain::{Context, MarkdownRunnable};

#[derive(Debug, PartialEq, Deserialize)]
pub(crate) struct DisplayFileEntry {
    path: String,
    working_dir: Option<String>,
    tags: Option<Vec<String>>,
}

impl MarkdownRunnable for DisplayFileEntry {
    fn to_markdown(&self, context: &mut Context) -> Result<String, String> {
        let file_type = match self.path.rsplit_once('.') {
            None => "",
            Some((_, extension)) => extension,
        };

        let p = match &self.working_dir {
            None => PathBuf::new(),
            Some(dir) => PathBuf::from(dir),
        }
        .join(&self.path);

        let content = fs::read_to_string(&p);
        if (content.is_err()) {
            return Err(format!("Failed to read the file {}", self.path));
        }

        let mut md = String::new();
        md.push_str("```");
        md.push_str(file_type);
        md.push_str("\n");
        md.push_str(content.unwrap().as_str());
        md.push_str("```\n");
        Ok(md)
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    mod deserialize_tests {
        use crate::domain::Document;
        use crate::domain::Entry::DisplayFile;

        use super::*;

        #[test]
        fn return_deserialized_markdown_when_given_minimum_options() {
            let json = r#"{
  "variables": [],
  "entries": [
    {
      "type": "DisplayFile",
      "working_dir": "some-directory",
      "path": "./some/path/File.java"
    }
  ]
}"#;

            let expected = Document {
                variables: vec![],
                entries: vec![DisplayFile(DisplayFileEntry {
                    path: "./some/path/File.java".to_string(),
                    working_dir: Some("some-directory".to_string()),
                    tags: None,
                })],
            };

            let deserialized: Document = Document::parse(json).unwrap();
            assert_eq!(expected, deserialized);
        }
    }

    mod markdown_runnable_tests {
        use std::fs::OpenOptions;
        use std::io::Write;

        use super::*;

        #[test]
        fn format_java_file() {
            let java_file = r#"package demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {

    public static void main(final String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
"#;

            let path = std::path::Path::new("./target/fixtures/Main.java");
            fs::create_dir_all(path.parent().expect("Failed to create test file"))
                .expect("Failed to create test file");

            let mut file = OpenOptions::new()
                .write(true)
                .truncate(true)
                .create(true)
                .open(path)
                .expect("Failed to create test file");

            write!(file, "{}", java_file).expect("Failed to create test file");

            /* Given */
            let entry = DisplayFileEntry {
                path: "./fixtures/Main.java".to_string(),
                working_dir: Some("target".to_string()),
                tags: None,
            };

            /* When */
            let md = entry.to_markdown(&mut Context::empty());

            /* Then */
            assert_eq!(
                Ok(r#"```java
package demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {

    public static void main(final String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
```
"#
                .to_string()),
                md
            );
        }
    }
}
