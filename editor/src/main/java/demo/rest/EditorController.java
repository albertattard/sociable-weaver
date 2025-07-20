package demo.rest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;

@Controller
public final class EditorController {

    private final List<EntryTo> entries = new ArrayList<>();

    public EditorController() {
        entries.add(EntryTo.heading("H2", "Test Heading"));
        entries.add(new EntryTo(EntryType.Markdown));
        entries.add(new EntryTo(EntryType.DisplayFile));
    }

    @GetMapping("/")
    public String index(final Model model) {
        model.addAttribute("entries", entries);
        return "index";
    }

    @PostMapping("/")
    public String add(final EntryTo entry, final Model model) {
        /* TODO: Add validation */
        /* TODO: Change the type and create the ID */
        entries.add(entry);
        model.addAttribute("entry", entry);
        return "fragments/entry :: renderEntry";
    }

    @PostMapping("/after")
    public String addAfter(final AddEntryAfterTo addEntryAfter, final Model model) {
        /* TODO: Add validation */
        /* TODO: Find the entry and add this after that entry */
        final EntryTo entry = addEntryAfter.toEntry();

        final int index = indexOfEntry(addEntryAfter.afterId())
                .orElseThrow(() -> new IllegalArgumentException("Entry with id " + addEntryAfter.afterId() + " was not found"));

        entries.add(index + 1, entry);
        model.addAttribute("entry", entry);
        return "fragments/entry :: renderEntry";
    }

    private OptionalInt indexOfEntry(final String entryId) {
        return Optional.ofNullable(entryId).stream()
                .flatMapToInt(id -> {
                    for (int i = 0; i < entries.size(); i++) {
                        if (id.equals(entries.get(i).id())) {
                            return IntStream.of(i);
                        }
                    }

                    return IntStream.empty();
                })
                .findFirst();
    }
}
