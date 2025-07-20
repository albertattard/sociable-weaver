package demo.rest;

import demo.domain.EntryTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public final class EditorController {

    private final List<EntryTo> entries = new ArrayList<>();

    public EditorController() {
        entries.add(EntryTo.heading("H2", "Test Heading"));
        entries.add(new EntryTo("Markdown"));
        entries.add(new EntryTo("DisplayFile"));
    }

    @GetMapping("/")
    public String index(final Model model) {
        model.addAttribute("entries", entries);
        return "index";
    }

    @PostMapping("/")
    public String add(final EntryTo entry, final Model model) {
        /* TODO: Add validation */
        entries.add(entry);
        model.addAttribute("entry", entry);
        return "fragments/entry :: renderEntry";
    }
}
