package edu.kit.hci.soli.config.template;

import gg.jte.TemplateOutput;
import gg.jte.html.HtmlTemplateOutput;
import gg.jte.html.OwaspHtmlTemplateOutput;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class JteSoliTemplateOutput extends OwaspHtmlTemplateOutput {
    private static final Set<String> nonNlTags = Set.of(
            "script", // Parent class has special handling for script tags.
            "textarea", // In HTML, the content of a textarea is taken as literal text. Do not insert line breaks.
            "pre", // In HTML, the content of a pre tag is taken as literal text. Do not insert line breaks.
            "style" // Style is not HTML, do not insert line breaks
    );

    private final TemplateOutput templateOutput;

    public JteSoliTemplateOutput(TemplateOutput templateOutput) {
        super(templateOutput);
        if (templateOutput instanceof HtmlTemplateOutput) {
            throw new IllegalArgumentException("JteSoliTemplateOutput must not be used with HtmlTemplateOutput");
        }
        this.templateOutput = templateOutput;
    }

    private String tagName;
    private String attributeName;

    @Override
    public void setContext(String tagName, String attributeName) {
        super.setContext(tagName, attributeName);
        this.tagName = tagName;
        this.attributeName = attributeName;
    }

    @Override
    public void writeUserContent(String value) {
        if (value == null) return; // Prevent NPEs. This is the same behavior as in OwaspHtmlTemplateOutput.
        if (tagName != null && attributeName != null) super.writeUserContent(value); // Attributes are handled by the parent class.
        else if (nonNlTags.contains(tagName)) super.writeUserContent(value); // Some tags do not allow line breaks using <br>.
        else {
            // If this is normal HTML, escape it using the parent class but with line breaks inserted.
            try (Stream<String> lines = value.lines()) {
                AtomicBoolean first = new AtomicBoolean(true);
                lines.forEach(line -> {
                    if (!first.getAndSet(false)) templateOutput.writeUserContent("<br>");
                    super.writeUserContent(line);
                });
            }
        }
    }
}
