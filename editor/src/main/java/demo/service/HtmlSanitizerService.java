package demo.service;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Service;

@Service
public class HtmlSanitizerService {

    private final PolicyFactory policy;

    public HtmlSanitizerService() {
        this.policy = new HtmlPolicyBuilder()
                .allowElements("p", "a", "ul", "ol", "li", "strong", "em", "code", "pre", "blockquote", "h1", "h2", "h3", "h4", "h5", "h6")
                .allowUrlProtocols("http", "https")
                .allowAttributes("href").onElements("a")
                .requireRelNofollowOnLinks()
                .toFactory();
    }

    public String sanitize(final String unsafeHtml) {
        return policy.sanitize(unsafeHtml);
    }
}