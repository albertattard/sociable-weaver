package demo;

import demo.rest.EntryType;
import demo.rest.HeadingLevel;
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
                    .assertTitleContains("New Heading 2");
        }
    }

    @Test
    void editAndCancelFirstHeadingEntry() {
        try (EditorWebApplication editor = EditorWebApplication.launch()) {
            editor.openEditorPage()
                    .row(0)
                    .assertTitleContains("Test Heading")
                    .clickEditButton()
                    .waitForEditFormToBeVisible()
                    .setTitle("New Heading")
                    .clickCancelButton()
                    .assertTitleContains("Test Heading");
        }
    }

    @Test
    void updateFirstHeadingEntry() {
        try (EditorWebApplication editor = EditorWebApplication.launch()) {
            editor.openEditorPage()
                    .row(0)
                    .clickEditButton()
                    .waitForEditFormToBeVisible()
                    .assertHeadingFieldsVisible()
                    .setTitle("Updated Heading")
                    .clickUpdateButton()
                    .assertTitleContains("Updated Heading");
        }
    }

    @Test
    void deleteSecondEntry() {
        try (EditorWebApplication editor = EditorWebApplication.launch()) {
            editor.openEditorPage()
                    .row(1)
                    .clickDeleteButton()
                    .row(1)
                    .assertContains("DisplayFile");
        }
    }
}
