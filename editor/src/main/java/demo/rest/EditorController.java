package demo.rest;

import demo.domain.Document;
import demo.service.HtmlConverterService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;

@Controller
public final class EditorController {

    @Value("${playbook:sw-runbook.json}")
    private Path playbook;

    private final List<BigEntryTo> entries = new ArrayList<>();

    /* Can only delete the last deleted entry */
    private DeletedEntry lastDeletedEntry;

    private final HtmlConverterService htmlConverterService;

    public EditorController(final HtmlConverterService htmlConverterService) {
        this.htmlConverterService = requireNonNull(htmlConverterService, "The markdown service cannot be null");
    }

    @PostConstruct
    public void init() {
        if (playbook != null && Files.exists(playbook)) {
            Document.parse(playbook).entries().stream()
                    .map(BigEntryTo::of)
                    .forEach(entries::add);
        } else {
            /* TODO: Show a warning on the page instead */
        }
    }

    @GetMapping("/")
    public String index(final Model model) {
        model.addAttribute("entries", entries.stream().map(htmlConverterService::toView).toList());
        return "index";
    }

    @GetMapping("/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}")
    public String get(final @PathVariable("id") UUID id, final Model model) {
        final BigEntryTo entry = findEntryWithId(id)
                .orElseThrow(() -> new IllegalArgumentException("Entry with id " + id + " was not found"));

        model.addAttribute("entry", htmlConverterService.toView(entry));
        return "fragments/entry :: renderEntry";
    }

    @PostMapping("/after")
    public String addAfter(final AddEntryAfterTo addEntryAfter, final Model model) {
        /* TODO: Add validation */
        final BigEntryTo entry = addEntryAfter.toNewEntry();

        final int index = indexOfEntry(addEntryAfter.id())
                .orElseThrow(() -> new IllegalArgumentException("Entry with id " + addEntryAfter.id() + " was not found"));

        entries.add(index + 1, entry);
        model.addAttribute("entry", entry);
        return "fragments/entry :: editEntry";
    }

    @PostMapping("/edit")
    public String update(final BigEntryTo entry, final Model model) {
        /* TODO: Add validation */
        final int index = indexOfEntry(entry.id())
                .orElseThrow(() -> new IllegalArgumentException("Entry with id " + entry.id() + " was not found"));

        entries.set(index, entry);
        model.addAttribute("entry", htmlConverterService.toView(entry));
        return "fragments/entry :: renderEntry";
    }

    @GetMapping("/edit")
    public String edit(final @RequestParam("id") UUID id, final Model model) {
        final BigEntryTo entry = findEntryWithId(id)
                .orElseThrow(() -> new IllegalArgumentException("Entry with id " + id + " was not found"));
        model.addAttribute("entry", entry);
        return "fragments/entry :: editEntry";
    }

    @DeleteMapping("/delete")
    public String delete(final @RequestParam("id") UUID id, final Model model) {
        final int index = indexOfEntry(id)
                .orElseThrow(() -> new IllegalArgumentException("Entry with id " + id + " was not found"));
        lastDeletedEntry = new DeletedEntry(entries.remove(index), index);
        model.addAttribute("id", id);
        return "fragments/entry :: undoDelete";
    }

    @PostMapping("/undo")
    public String undo(final @RequestParam("id") UUID id, final Model model) {
        if (lastDeletedEntry == null || !id.equals(lastDeletedEntry.entry().id())) {
            model.addAttribute("id", id);
            return "fragments/entry :: cannotUndoDelete";
        }

        entries.add(lastDeletedEntry.index(), lastDeletedEntry.entry());
        model.addAttribute("entry", htmlConverterService.toView(lastDeletedEntry.entry()));
        lastDeletedEntry = null;
        return "fragments/entry :: renderEntry";
    }

    private Optional<BigEntryTo> findEntryWithId(final UUID entryId) {
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
