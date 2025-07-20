package demo.rest;

import java.util.UUID;

public record AddEntryAfterTo(
        String afterId,
        String type) {

    public EntryTo toEntry() {
        return switch (type) {
            case "Heading" ->
                    new EntryTo(UUID.randomUUID().toString(), type, null, null, null, "H2", "Heading...", null);
            default -> new EntryTo(UUID.randomUUID().toString(), type, null, null, null, null, null, null);
        };
    }
}
