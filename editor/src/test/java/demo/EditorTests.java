package demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EditorTests {

    @LocalServerPort
    private int port;

    @Test
    void addEntryToTheEndOfTheList() {
        try (EditorWebApplication editor = EditorWebApplication.create(port)) {
            editor.openEditorPage()
                    .addItem("Mango")
                    .assertLastItemIs("Mango");
        }
    }
}
