use std::fs;

use serde::Deserialize;

use crate::domain::MarkdownRunnable;

#[derive(Debug, PartialEq, Deserialize)]
pub(crate) struct DisplayFileEntry {
    path: String,
    from_line: Option<usize>,
    number_of_lines: Option<usize>,
    tags: Option<Vec<String>>,
}

impl MarkdownRunnable for DisplayFileEntry {
    fn run_markdown(&self) -> Result<String, String> {
        let file_type = match self.path.rsplit_once('.') {
            None => "",
            Some((_, extension)) => extension,
        };

        let content = fs::read_to_string(&self.path);
        if content.is_err() {
            return Err(format!("Failed to read the file {}", self.path));
        }

        let mut content = content.expect(&format!("Failed to read the file {}", self.path));
        if let Some(from_base_1) = self.from_line {
            let skip_n_lines = content.lines().skip(from_base_1 - 1);

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

        let md = format!("```{file_type}\n{content}```\n");
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
      "path": "./some/path/File.java"
    }
  ]
}"#;

            let expected = Document {
                entries: vec![DisplayFile(DisplayFileEntry {
                    path: "./some/path/File.java".to_string(),
                    from_line: Some(5),
                    number_of_lines: Some(3),
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
        fn run_java_file() {
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

            write_fixture("./target/fixtures/1/Main.java", java_file);

            /* Given */
            let entry = DisplayFileEntry {
                path: "./target/fixtures/1/Main.java".to_string(),
                from_line: None,
                number_of_lines: None,
                tags: None,
            };

            /* When */
            let md = entry.run_markdown();

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
        fn run_java_lines_from_file() {
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

            write_fixture("./target/fixtures/2/Main.java", java_file);

            /* Given */
            let entry = DisplayFileEntry {
                path: "./target/fixtures/2/Main.java".to_string(),
                from_line: Some(9),
                number_of_lines: Some(3),
                tags: None,
            };

            /* When */
            let md = entry.run_markdown();

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
