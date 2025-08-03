package demo.service;

import demo.rest.BigEntryTo;
import demo.rest.ViewEntryTo;
import org.springframework.stereotype.Service;

import static java.util.Objects.requireNonNull;

@Service
public class HtmlConverterService {

    private final MarkdownService markdownService;

    public HtmlConverterService(final MarkdownService markdownService) {
        requireNonNull(markdownService, "The markdown service cannot be null");
        this.markdownService = markdownService;
    }

    public ViewEntryTo toView(final BigEntryTo edit) {
        final String markdown = switch (edit.type()) {
            case Breakpoint -> edit.comments();
            case Command -> "Command";
            case DisplayFile -> "DisplayFile";
            case Heading -> {
                final String h = switch (edit.level()) {
                    case H1 -> "#";
                    case H2 -> "##";
                    case H3 -> "###";
                    case H4 -> "####";
                    case H5 -> "#####";
                };
                yield h + ' ' + edit.title();
            }
            case Markdown -> edit.contents();
            case Todo -> "Todo";
        };

        return new ViewEntryTo(edit.id(), toHtml(markdown));
    }

    private String toHtml(final String markdown) {
        return markdownService.render(markdown);
    }
}
