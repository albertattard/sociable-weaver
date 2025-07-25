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
    void editAndCancelFirstHeadingEntry() {
        try (EditorWebApplication editor = EditorWebApplication.launch()) {
            editor.openEditorPage()
                    .assertElementAtIndexContains(0, "> h2", "Test Heading")
                    .clickOnElementAtIndex(0, "> button[name=edit]")
                    .waitForElementToBeVisible(0, "> form > button[name=cancel]")
                    .clickOnElementAtIndex(0, "> form > button[name=cancel]")
                    .assertElementAtIndexContains(0, "> h2", "Test Heading");
        }
    }

    @Test
    void fieldsVisibleWhenEditingHeadingEntry() {
        try (EditorWebApplication editor = EditorWebApplication.launch()) {
            editor.openEditorPage()
                    .clickOnElementAtIndex(0, "> button[name=edit]")
                    .waitForElementToBeVisible(0, "> form > select[name=type]")
                    .assertElementAtIndexVisible(0, "> form > select[name=type]")
                    .assertElementAtIndexVisible(0, "> form > div#fields select[name=level]")
                    .assertElementAtIndexVisible(0, "> form > div#fields input[name=title]")
            ;
        }
    }

    @Test
    void updateFirstHeadingEntry() {
        try (EditorWebApplication editor = EditorWebApplication.launch()) {
            editor.openEditorPage()
                    .clickOnElementAtIndex(0, "> button[name=edit]")
                    .waitForElementToBeVisible(0, "> form input[name=title]")
                    .setInputValueAtIndex(0, "> form input[name=title]", "Updated Heading")
                    .clickOnElementAtIndex(0, "> form > button[name=update]")
                    .assertElementAtIndexContains(0, "> h2", "Updated Heading");
        }
    }
}
