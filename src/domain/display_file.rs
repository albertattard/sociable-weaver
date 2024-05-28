use std::fs;
use std::path::PathBuf;

use serde::Deserialize;

use crate::domain::{Context, MarkdownRunnable};

#[derive(Debug, PartialEq, Deserialize)]
pub(crate) struct DisplayFileEntry {
    path: String,
    from_line: Option<usize>,
    number_of_lines: Option<usize>,
    working_dir: Option<String>,
    tags: Option<Vec<String>>,
}

impl MarkdownRunnable for DisplayFileEntry {
    fn to_markdown(&self, _context: &mut Context) -> Result<String, String> {
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
        if content.is_err() {
            return Err(format!("Failed to read the file {}", self.path));
        }

        let mut content = content.expect(&format!("Failed to read the file {}", self.path));
        if let Some(from) = self.from_line {
            let skip_n_lines = content.lines().skip(from);

            content = if let Some(n) = self.number_of_lines {
                skip_n_lines
                    .take(n)
                    .map(|line| format!("{}\n", line))
                    .collect()
            } else {
                skip_n_lines.map(|line| format!("{}\n", line)).collect()
            };
        } else if let Some(n) = self.number_of_lines {
            content = content
                .lines()
                .take(n)
                .map(|line| format!("{}\n", line))
                .collect()
        }

        let mut md = String::new();
        md.push_str("```");
        md.push_str(file_type);
        md.push_str("\n");
        md.push_str(content.as_str());
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
  "entries": [
    {
      "type": "DisplayFile",
      "path": "./some/path/File.java"
    }
  ]
}"#;

            let expected = Document {
                entries: vec![DisplayFile(DisplayFileEntry {
                    path: "./some/path/File.java".to_string(),
                    from_line: None,
                    number_of_lines: None,
                    working_dir: None,
                    tags: None,
                })],
            };

            let deserialized: Document = Document::parse(json).unwrap();
            assert_eq!(expected, deserialized);
        }

        #[test]
        fn return_deserialized_markdown_when_given_all_options() {
            let json = r#"{
  "entries": [
    {
      "type": "DisplayFile",
      "from_line": 5,
      "number_of_lines": 3,
      "working_dir": "some-directory",
      "path": "./some/path/File.java"
    }
  ]
}"#;

            let expected = Document {
                entries: vec![DisplayFile(DisplayFileEntry {
                    path: "./some/path/File.java".to_string(),
                    from_line: Some(5),
                    number_of_lines: Some(3),
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
        use std::path::Path;

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

            let path = write_fixture("./target/fixtures/Main.java", java_file);

            /* Given */
            let entry = DisplayFileEntry {
                path: "./fixtures/Main.java".to_string(),
                from_line: None,
                number_of_lines: None,
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

        #[test]
        fn format_java_lines_from_file() {
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

            let path = write_fixture("./target/fixtures/Main.java", java_file);

            /* Given */
            let entry = DisplayFileEntry {
                path: "./fixtures/Main.java".to_string(),
                from_line: Some(8),
                number_of_lines: Some(3),
                working_dir: Some("target".to_string()),
                tags: None,
            };

            /* When */
            let md = entry.to_markdown(&mut Context::empty());

            /* Then */
            assert_eq!(
                Ok(r#"```java
    public static void main(final String[] args) {
        SpringApplication.run(Main.class, args);
    }
```
"#
                .to_string()),
                md
            );
        }

        fn write_fixture<'a>(path: &'a str, content: &str) -> &'a Path {
            let path = Path::new(path);

            fs::create_dir_all(path.parent().expect("Failed to create fixture file"))
                .expect("Failed to create fixture file");

            let mut file = OpenOptions::new()
                .write(true)
                .truncate(true)
                .create(true)
                .open(path)
                .expect("Failed to create test file");

            write!(file, "{}", content).expect("Failed to create fixture file");

            path
        }
    }
}
