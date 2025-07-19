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
        entries.add(new EntryTo("Apple"));
        entries.add(new EntryTo("Banana"));
        entries.add(new EntryTo("Orange"));
    }

    @GetMapping("/")
    public String index(final Model model) {
        model.addAttribute("entries", entries);
        return "index";
    }

    @PostMapping("/")
    public String addEntry(final @RequestParam String name, final Model model) {
        final EntryTo newEntry = new EntryTo(name);
        entries.add(newEntry);
        model.addAttribute("entry", newEntry);
        return "fragments/entry :: entryRow";
    }
}
