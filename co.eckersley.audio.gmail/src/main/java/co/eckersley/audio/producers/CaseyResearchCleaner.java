package co.eckersley.audio.producers;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CaseyResearchCleaner extends HtmlCleaner {

//    private final String CONTENT_CLASS_1 = "content";
    private final String CONTENT_TAG_1 = "strong";
    private final String CONTENT_TAG_2 = "p";
    private final String CONTENT_TAG_3 = "td";
    private final String CONTENT_TAG_4 = "li";
    
    private final List<String> CONTENT_TAGS = Arrays.asList(CONTENT_TAG_1, CONTENT_TAG_2, CONTENT_TAG_3, CONTENT_TAG_4);
    
    private final String CONTENT_END_MARKER_1 = "Recent Dispatches";

    public String clean(String from, String subject, Document doc) {

        // Elements contents = doc.body().getElementsByClass(CONTENT_CLASS_1);
        Elements contents = doc.body().children();

        StringBuilder output = new StringBuilder();

        parse(contents, output, subject);

        return output.toString();
    }
    
    private boolean skipTitle = true;
    private boolean skipRemaining = false;
    
    private void parse(Elements contents, StringBuilder output, String subject) {
        
        for (Element e : contents) {
            if (e.children().size() > 0 && !CONTENT_TAGS.contains(e.tagName())) {
                logger.debug("Skipping <" + e.tagName() + ">: with children");
            } else {
                if (extract(e, subject)) {
                    logger.debug("Extracting <" + e.tagName() + ">: " + e.text());
                    extract(e, output);
                } else {
                    logger.debug("Skipping <" + e.tagName() + ">: " + e.text());
                }
            }
            parse(e.children(), output, subject);
        }
    }
    
    private boolean extract(Element e, String subject) {
        
        // Skip the Title the first time its seen.
        if (skipTitle && CONTENT_TAGS.contains(e.tagName()) && e.hasText() && e.text().length() > 5 && StringUtils.containsIgnoreCase(subject, e.text())) {
            skipTitle = false;
            return false;
        }
            
        // Skip the remaining tags once the end marker is seen.
        if (CONTENT_TAGS.contains(e.tagName()) && e.text().contains(CONTENT_END_MARKER_1) && e.children().size() == 0) {
            skipRemaining = true;
            return false;
        }
            
        // Skip if this text is contained in the last output
        if (e.hasText() && e.text().length() > 3 && StringUtils.contains(lastOutput, e.text())) {
            return false;
        }
        
        // Include <td> on for the main heading
        if (CONTENT_TAG_3.matches(e.tagName())) {
            return (e.hasAttr("style") && e.attr("style").contains("font-size:36px"));
        }
        
        // Include <li> only if other content has been output.
        if (CONTENT_TAG_4.matches(e.tagName()) && e.hasText()) {
            return lastOutput != null;
        }
        
        if (!skipRemaining && e.hasText() && CONTENT_TAGS.contains(e.tagName())) {
            return true;
        }
        
        return false;

    }
    
    private String lastOutput;
    
    private void extract(Element e, StringBuilder output) {
        lastOutput = e.text();
        output.append(lastOutput).append(SystemUtils.LINE_SEPARATOR).append(SystemUtils.LINE_SEPARATOR);
    }
}
