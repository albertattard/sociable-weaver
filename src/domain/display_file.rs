use std::fs;
use std::path::PathBuf;

use serde::Deserialize;

use crate::domain::MarkdownRunnable;
use crate::utils::strings::indent_by;

#[derive(Debug, PartialEq, Deserialize)]
pub(crate) struct DisplayFileEntry {
    path: String,
    content_type: Option<String>,
    from_line: Option<usize>,
    number_of_lines: Option<usize>,
    tags: Option<Vec<String>>,
    indent: Option<usize>,
}

impl DisplayFileEntry {
    fn add_indent(&self, markdown: String) -> String {
        indent_by(markdown, &self.indent)
    }

    fn path(&self) -> PathBuf {
        let path = &self.path;

        if path.starts_with("~") {
            if let Some(home_dir) = dirs::home_dir() {
                let stripped_path = if path.starts_with("~/") {
                    path.strip_prefix("~/").unwrap()
                } else {
                    path.strip_prefix("~").unwrap()
                };

                return home_dir.join(stripped_path);
            }
        }

        return PathBuf::from(path);
    }
}

impl MarkdownRunnable for DisplayFileEntry {
    fn run_markdown(&self) -> Result<String, String> {
        let path = &self.path();

        let file_type = match &self.content_type {
            None => match path.extension() {
                None => "",
                Some(extension) => extension.to_str().unwrap_or_else(|| ""),
            },
            Some(c) => c,
        };

        let content = fs::read_to_string(path);
        if content.is_err() {
            return Err(format!("Failed to read the file {:?}", path));
        }

        let mut content = content.expect(&format!("Failed to read the file {:?}", path));
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

        /* Append a new-line at the end if the content does not have that otherwise the markdown
        closing '```' will be on the same line as the last line from the file. */
        if !content.ends_with('\n') {
            content.push('\n');
        }

        let md = format!("```{file_type}\n{content}```\n");
        Ok(self.add_indent(md))
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

            let expected = Document::new(vec![DisplayFile(DisplayFileEntry {
                path: "./some/path/File.java".to_string(),
                content_type: None,
                from_line: None,
                number_of_lines: None,
                tags: None,
                indent: None,
            })]);

            let deserialized: Document = Document::parse(json).unwrap();
            assert_eq!(expected, deserialized);
        }

        #[test]
        fn return_deserialized_markdown_when_given_all_options() {
            let json = r#"{
  "entries": [
    {
      "type": "DisplayFile",
      "content_type": "something",
      "from_line": 5,
      "number_of_lines": 3,
      "path": "./some/path/File.java"
    }
  ]
}"#;

            let expected = Document::new(vec![DisplayFile(DisplayFileEntry {
                path: "./some/path/File.java".to_string(),
                content_type: Some("something".to_string()),
                from_line: Some(5),
                number_of_lines: Some(3),
                tags: None,
                indent: None,
            })]);

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
                content_type: None,
                from_line: None,
                number_of_lines: None,
                tags: None,
                indent: None,
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
        fn run_java_file_without_new_line_at_the_end() {
            let java_file = r#"package demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {

    public static void main(final String[] args) {
        SpringApplication.run(Main.class, args);
    }
}"#; // No new-line at the end

            write_fixture("./target/fixtures/2/Main.java", java_file);

            /* Given */
            let entry = DisplayFileEntry {
                path: "./target/fixtures/2/Main.java".to_string(),
                content_type: None,
                from_line: None,
                number_of_lines: None,
                tags: None,
                indent: None,
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

            write_fixture("./target/fixtures/3/Main.java", java_file);

            /* Given */
            let entry = DisplayFileEntry {
                path: "./target/fixtures/3/Main.java".to_string(),
                content_type: None,
                from_line: Some(9),
                number_of_lines: Some(3),
                tags: None,
                indent: None,
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

        #[test]
        fn run_java_lines_from_file_with_different_content_type() {
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

            write_fixture("./target/fixtures/4/Main.java", java_file);

            /* Given */
            let entry = DisplayFileEntry {
                path: "./target/fixtures/4/Main.java".to_string(),
                content_type: Some("txt".to_string()),
                from_line: Some(9),
                number_of_lines: Some(3),
                tags: None,
                indent: None,
            };

            /* When */
            let md = entry.run_markdown();

            /* Then */
            assert_eq!(
                Ok(r#"```txt
    public static void main(final String[] args) {
        SpringApplication.run(Main.class, args);
    }
```
"#
                .to_string()),
                md
            );
        }

        #[test]
        fn run_java_lines_from_file_with_indentation() {
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

            write_fixture("./target/fixtures/5/Main.java", java_file);

            /* Given */
            let entry = DisplayFileEntry {
                path: "./target/fixtures/5/Main.java".to_string(),
                content_type: None,
                from_line: Some(9),
                number_of_lines: Some(3),
                tags: None,
                indent: Some(3),
            };

            /* When */
            let md = entry.run_markdown();

            /* Then */
            assert_eq!(
                Ok(r#"   ```java
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

    mod path_test {
        use super::*;

        #[test]
        fn convert_tilda_into_user_home_directory() {
            let entry = DisplayFileEntry {
                path: "~/.m2/toolchains.xml".to_string(),
                content_type: None,
                from_line: None,
                number_of_lines: None,
                tags: None,
                indent: None,
            };

            if let Some(home_dir) = dirs::home_dir().unwrap().to_str() {
                let expected = PathBuf::from(format!("{home_dir}/.m2/toolchains.xml"));
                assert_eq!(expected, entry.path())
            }
        }
    }
}
