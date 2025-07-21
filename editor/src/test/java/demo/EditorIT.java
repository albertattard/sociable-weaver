package demo;

import demo.rest.EntryType;
import org.junit.jupiter.api.Test;

class EditorIT {

    @Test
    void addHeadingAfterAnotherEntry() {
        try (EditorWebApplication editor = EditorWebApplication.launch()) {
            editor.openEditorPage()
                    .addAfter(EntryType.Heading, 1)
                    .assertElementAtIndexContains(2, "> h2", "Heading...");
        }
    }

    @Test
    void editFirstHeadingEntry() {
        try (EditorWebApplication editor = EditorWebApplication.launch()) {
            editor.openEditorPage()
                    .assertElementAtIndexContains(0, "> h2", "Test Heading")
                    .clickOnElementAtIndex(0, "> button[name=edit]")
                    .clickOnElementAtIndex(0, "> form > button[name=cancel]")
                    .assertElementAtIndexContains(0, "> h2", "Test Heading");
        }
    }
}
