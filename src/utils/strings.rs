pub(crate) fn indent_by(text: String, indent: &Option<usize>) -> String {
    match indent {
        None => text,
        Some(indentation) => {
            let padding = " ".repeat(*indentation);

            let mut indented = String::new();
            let mut lines = text.split("\n");

            if let Some(first_line) = lines.next() {
                pad_and_append_if_not_empty(&padding, first_line, &mut indented);
            }

            for line in lines {
                indented.push('\n');
                pad_and_append_if_not_empty(&padding, line, &mut indented);
            }

            indented
        }
    }
}

#[inline(always)]
fn pad_and_append_if_not_empty(padding: &str, line: &str, indented: &mut String) {
    if !line.is_empty() {
        indented.push_str(padding);
        indented.push_str(line);
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
        let text = "\nHello\n\nWorld".to_string();
        let result = indent_by(text, &Some(4));
        let expected = "\n    Hello\n\n    World";
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
