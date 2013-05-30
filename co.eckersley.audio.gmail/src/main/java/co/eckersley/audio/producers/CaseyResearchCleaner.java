package co.eckersley.audio.producers;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CaseyResearchCleaner extends HtmlCleaner {

    private final String CONTENT_CLASS_1 = "content";

    public String clean(String from, String subject, Document doc) {

        Elements contents = doc.body().getElementsByClass(CONTENT_CLASS_1);

        StringBuilder output = new StringBuilder();

        for (Element e : contents) {
            output.append(e.text());
        }

        return output.toString();
    }
}
