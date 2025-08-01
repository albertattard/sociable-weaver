package demo;

import demo.domain.Heading.HeadingLevel;
import demo.rest.EntryType;
import org.junit.jupiter.api.Test;

class EditorIT {

    @Test
    void addHeadingAfterAnotherEntry() {
        try (EditorWebApplication editor = EditorWebApplication.launch()) {
            editor.openEditorPage()
                    .row(1)
                    .addAfter(EntryType.Heading)
                    .waitForEditFormToBeVisible()
                    .assertHeadingFieldsVisible()
                    .setTitle("New Heading 2")
                    .selectLevel(HeadingLevel.H2)
                    .clickUpdateButton()
                    .assertTitleContains(HeadingLevel.H2, "New Heading 2");
        }
    }

    @Test
    void editAndCancelFirstHeadingEntry() {
        try (EditorWebApplication editor = EditorWebApplication.launch()) {
            editor.openEditorPage()
                    .row(0)
                    .assertTitleContains(HeadingLevel.H2, "Test Heading")
                    .clickEditButton()
                    .waitForEditFormToBeVisible()
                    .setTitle("New Heading")
                    .clickCancelButton()
                    .waitForHeadingToBeVisible(HeadingLevel.H2)
                    .assertTitleContains(HeadingLevel.H2, "Test Heading");
        }
    }

    @Test
    void updateFirstHeadingEntry() {
        try (EditorWebApplication editor = EditorWebApplication.launch()) {
            editor.openEditorPage()
                    .row(0)
                    .clickEditButton()
                    .waitForEditHeadingToBeVisible()
                    .assertHeadingFieldsVisible()
                    .assertTitleContains("Test Heading")
                    .setTitle("Updated Heading")
                    .clickUpdateButton()
                    .assertTitleContains(HeadingLevel.H2, "Updated Heading");
        }
    }

    @Test
    void deleteSecondEntry() {
        try (EditorWebApplication editor = EditorWebApplication.launch()) {
            editor.openEditorPage()
                    .row(1)
                    .clickDeleteButton()
                    .assertRowTextContains("Entry deleted");
        }
    }

    @Test
    void deleteAndUndoSecondEntry() {
        try (EditorWebApplication editor = EditorWebApplication.launch()) {
            editor.openEditorPage()
                    .row(1)
                    .clickDeleteButton()
                    .assertRowTextContains("Entry deleted")
                    .clickUndoButton()
                    .assertRowTextContains("Markdown");
        }
    }

    @Test
    void deleteMultipleEntriesAndUndoFirstDeletedEntry() {
        try (EditorWebApplication editor = EditorWebApplication.launch()) {
            editor.openEditorPage()
                    .row(1)
                    .assertRowTextContains("Markdown")
                    .clickDeleteButton()
                    .row(2)
                    .assertRowTextContains("DisplayFile")
                    .clickDeleteButton()
                    .row(1)
                    .clickUndoButton()
                    .assertRowTextContains("Deletion cannot be undone")
                    .row(2)
                    .clickUndoButton()
                    .assertRowTextContains("DisplayFile");
        }
    }
}
