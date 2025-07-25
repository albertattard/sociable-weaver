package demo.rest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.IntStream;

@Controller
public final class EditorController {

    private final List<EntryTo> entries = new ArrayList<>();

    public EditorController() {
        entries.add(EntryTo.heading(HeadingLevel.H2, "Test Heading"));
        entries.add(new EntryTo(EntryType.Markdown));
        entries.add(new EntryTo(EntryType.DisplayFile));
    }

    @GetMapping("/")
    public String index(final Model model) {
        model.addAttribute("entries", entries);
        return "index";
    }

    @GetMapping("/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}")
    public String get(final @PathVariable("id") UUID id, final Model model) {
        final EntryTo entry = findEntryWithId(id)
                .orElseThrow(() -> new IllegalArgumentException("Entry with id " + id + " was not found"));

        model.addAttribute("entry", entry);
        return "fragments/entry :: renderEntry";
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
        final EntryTo entry = addEntryAfter.toNewEntry();

        final int index = indexOfEntry(addEntryAfter.id())
                .orElseThrow(() -> new IllegalArgumentException("Entry with id " + addEntryAfter.id() + " was not found"));

        entries.add(index + 1, entry);
        model.addAttribute("entry", entry);
        return "fragments/entry :: renderEntry";
    }

    @PostMapping("/edit")
    public String update(final EntryTo entry, final Model model) {
        /* TODO: Add validation */
        final int index = indexOfEntry(entry.id())
                .orElseThrow(() -> new IllegalArgumentException("Entry with id " + entry.id() + " was not found"));

        entries.set(index, entry);
        model.addAttribute("entry", entry);
        return "fragments/entry :: renderEntry";
    }

    @GetMapping("/edit")
    public String edit(final @RequestParam("id") UUID id, final Model model) {
        final EntryTo entry = findEntryWithId(id)
                .orElseThrow(() -> new IllegalArgumentException("Entry with id " + id + " was not found"));
        model.addAttribute("entry", entry);
        return "fragments/entry :: editEntry";
    }

    private Optional<EntryTo> findEntryWithId(final UUID entryId) {
        return Optional.ofNullable(entryId)
                .flatMap(id -> entries.stream().filter(e -> id.equals(e.id())).findFirst());
    }

    private OptionalInt indexOfEntry(final UUID entryId) {
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
