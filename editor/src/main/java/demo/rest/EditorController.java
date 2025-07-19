package demo.rest;

import demo.domain.EntryTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public final class EditorController {

    private final List<EntryTo> entries = new ArrayList<>();

    public EditorController() {
        entries.add(new EntryTo("Heading"));
        entries.add(new EntryTo("Markdown"));
        entries.add(new EntryTo("DisplayFile"));
    }

    @GetMapping("/")
    public String index(final Model model) {
        model.addAttribute("entries", entries);
        return "index";
    }

    @PostMapping("/")
    public String addEntry(final @RequestParam String type, final Model model) {
        final EntryTo newEntry = new EntryTo(type);
        entries.add(newEntry);
        model.addAttribute("entry", newEntry);
        return "fragments/entry :: entryRow";
    }
}
