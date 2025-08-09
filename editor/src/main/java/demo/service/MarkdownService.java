package demo.service;

import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Service
public class MarkdownService {

    private final Parser parser;
    private final HtmlRenderer renderer;
    private final HtmlSanitizerService sanitizer;

    public MarkdownService(final HtmlSanitizerService sanitizer) {
        requireNonNull(sanitizer, "The html sanitizer cannot be null");

        final MutableDataSet options = new MutableDataSet()
                .set(Parser.EXTENSIONS, List.of(TablesExtension.create()));
        this.parser = Parser.builder(options).build();
        this.renderer = HtmlRenderer.builder(options).build();
        this.sanitizer = sanitizer;
    }

    public String render(final String markdown) {
        final Document parsed = parser.parse(markdown);
        final String rawHtml = renderer.render(parsed);
        return sanitizer.sanitize(rawHtml);
    }
}
