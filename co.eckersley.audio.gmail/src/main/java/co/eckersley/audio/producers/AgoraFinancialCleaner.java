package co.eckersley.audio.producers;

import org.apache.commons.lang3.SystemUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class AgoraFinancialCleaner extends HtmlCleaner {

    private final String LAISSEZ_FAIRE_MARKER = "To end your Laissez Faire Today e-mail subscription";

    public String clean(String from, String subject, Document doc) {

        if (doc.body().text().contains(LAISSEZ_FAIRE_MARKER)) {
            return cleanLaissezFaire(doc);
        } else {
            return cleanAgoraFinancial(doc);
        }
    }
    
    private String cleanAgoraFinancial(Document doc) {

        Elements tables = doc.body().getElementsByTag("table");

        StringBuilder output = new StringBuilder();

        for (Element e : tables) {
            if ("100%".equals(e.attr("width")) && !e.text().contains("daily e-mail service brought to you by the publishers at Agora Financial"))
                output.append(e.text()).append(SystemUtils.LINE_SEPARATOR);
        }

        return output.toString();
    }
    
    private String cleanLaissezFaire(Document doc) {
        
        // <div class="content">

        Elements trs = doc.body().getElementsByTag("tr");

        StringBuilder output = new StringBuilder();

        for (Element e : trs) {
            if ("background:#FFF;".equals(e.attr("style")))
                output.append(e.text()).append(SystemUtils.LINE_SEPARATOR);
        }

        return output.toString();
    }
}
