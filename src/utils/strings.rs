pub(crate) fn indent_by(text: String, indent: &Option<usize>) -> String {
    match indent {
        None => text,
        Some(indentation) => {
            let mut indented = String::new();
            for line in text.split("\n") {
                if line.is_empty() {
                    indented.push('\n');
                } else {
                    indented.push_str(&format!(
                        "{:>indented_length$}\n",
                        line,
                        indented_length = line.chars().count() + indentation
                    ));
                }
            }

            /* A new-line is automatically appended which will result in an extra new-line at the end */
            indented.remove(indented.len() - 1);

            indented
        }
    }
}

#[cfg(test)]
mod tests {
    use super::indent_by;

    #[test]
    fn return_the_same_string_when_provided_no_indentation() {
        let text = "Hello\nWorld".to_string();
        let result = indent_by(text, &None);
        assert_eq!(result, "Hello\nWorld");
    }

    #[test]
    fn return_the_text_indented_by_four_spaces() {
        let text = "Hello\nWorld".to_string();
        let result = indent_by(text, &Some(4));
        let expected = "    Hello\n    World";
        assert_eq!(result, expected);
    }

    #[test]
    fn return_the_text_indented_by_four_spaces_skipping_blank_lines() {
        let text = "Hello\n\nWorld".to_string();
        let result = indent_by(text, &Some(4));
        let expected = "    Hello\n\n    World";
        assert_eq!(result, expected);
    }

    #[test]
    fn return_the_text_indented_by_four_spaces_preserve_trailing_new_line() {
        let text = "Hello\nWorld\n".to_string();
        let result = indent_by(text, &Some(4));
        let expected = "    Hello\n    World\n";
        assert_eq!(result, expected);
    }

    #[test]
    fn return_the_unicode_text_indented_by_four_spaces() {
        let text = "Hello\nKöln".to_string();
        let result = indent_by(text, &Some(4));
        let expected = "    Hello\n    Köln";
        assert_eq!(result, expected);
    }
}
