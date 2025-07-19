package demo;

import org.junit.jupiter.api.Test;

class EditorIT {

    @Test
    void addEntryToTheEndOfTheList() {
        try (EditorWebApplication editor = EditorWebApplication.launch()) {
            editor.openEditorPage()
                    .addItem("Mango")
                    .assertLastItemIs("Mango");
        }
    }
}
