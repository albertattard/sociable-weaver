package demo;

import org.junit.jupiter.api.Test;

class EditorIT {

    @Test
    void addHeadingToTheEndOfTheList() {
        try (EditorWebApplication editor = EditorWebApplication.launch()) {
            editor.openEditorPage()
                    .addHeading("H2", "Heading 2")
                    .assertLastEntryContains("> h2", "Heading 2");
        }
    }

    @Test
    void addMarkdownToTheEndOfTheList() {
        try (EditorWebApplication editor = EditorWebApplication.launch()) {
            editor.openEditorPage()
                    .addEntry("Markdown")
                    .assertLastEntryContains("Markdown");
        }
    }
}
