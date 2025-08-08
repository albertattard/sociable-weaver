package demo;

import demo.domain.Heading.HeadingLevel;
import demo.web.EntryType;
import org.junit.jupiter.api.Test;

class EditorIT {

    @Test
    void addHeadingAfterTheFirstEntry() {
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
                    .assertTitleContains(HeadingLevel.H1, "Test Heading")
                    .clickEditButton()
                    .waitForEditFormToBeVisible()
                    .setTitle("New Heading")
                    .clickCancelButton()
                    .waitForHeadingToBeVisible(HeadingLevel.H1)
                    .assertTitleContains(HeadingLevel.H1, "Test Heading");
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
                    .assertTitleContains(HeadingLevel.H1, "Updated Heading");
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
                    .assertRowTextContains("A simple example");
        }
    }

    @Test
    void deleteMultipleEntriesAndUndoFirstDeletedEntry() {
        try (EditorWebApplication editor = EditorWebApplication.launch()) {
            editor.openEditorPage()
                    .row(1)
                    .assertRowTextContains("A simple example")
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

    @Test
    void openPlaybookFromPath() {
        try (EditorWebApplication editor = EditorWebApplication.launch()) {
            editor.openEditorPage()
                    .open()
                    .setPlaybookPath("src/test/resources/fixtures/another-runbook.json")
                    .assertNoWarning()
                    .clickOpenButton()
                    .row(0)
                    .waitForHeadingToBeVisible(HeadingLevel.H1)
                    .assertTitleContains(HeadingLevel.H1, "Another Heading");
        }
    }

    @Test
    void showWarningWhenPlaybookDoesNotExist() {
        try (EditorWebApplication editor = EditorWebApplication.launch()) {
            editor.openEditorPage()
                    .open()
                    .setPlaybookPath("src/test/resources/fixtures/missing.json")
                    .clickOpenButton()
                    .assertWarningContains("does not exist");
        }
    }

    @Test
    void showWarningWhenFileIsNotAPlaybook() {
        try (EditorWebApplication editor = EditorWebApplication.launch()) {
            editor.openEditorPage()
                    .open()
                    .setPlaybookPath("src/test/resources/fixtures/not-a-playbook.txt")
                    .clickOpenButton()
                    .assertWarningContains("not a playbook");
        }
    }
}
