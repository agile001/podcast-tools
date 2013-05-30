package co.eckersley.audio.producers;

import org.apache.commons.lang3.SystemUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class FatProphetsCleaner extends HtmlCleaner {

    private final String FAT_WRAP_MARKER = "BENEFITS OF BECOMING A FAT PROPHETS MEMBER";
    private final String FAT_WEEKLY_MARKER = "Dear Member";

    private final String START_ADVERTISEMENT_MARKER = "--------------------------------------------------------------------------------------------------";
    private final String END_ADVERTISEMENT_MARKER = "--------------------------------------------------------------------------------------------------";
    

    public String clean(String from, String subject, Document doc) {

        if (doc.body().text().contains(FAT_WRAP_MARKER)) {
            return cleanFatWrap(doc);
        } else if (doc.body().text().contains(FAT_WEEKLY_MARKER)) {
            return cleanFatWeekly(doc);
        } else {
            return cleanFatDaily(doc);
        }
	}

    private String cleanFatDaily(Document doc) {

        Elements trs = doc.body().getElementsByTag("tr");

        StringBuilder output = new StringBuilder();
        boolean started = false;

        for (Element e : trs) {
            if (e.text().contains("Click here for the latest stock market news")) {
                if (started)
                    break;
            } else {
                started = true;
                output.append(e.text()).append(SystemUtils.LINE_SEPARATOR);
            }
        }

        // Strip DailyWealth Adverts
        int start = output.indexOf(START_ADVERTISEMENT_MARKER);
        int end = output.indexOf(END_ADVERTISEMENT_MARKER, start + START_ADVERTISEMENT_MARKER.length()) + END_ADVERTISEMENT_MARKER.length() + 1;
        if (start >= 0 && end >= 0) {
            output = new StringBuilder(strip(start, end, output.toString()));
        }
        

        return output.toString();
    }

    private String cleanFatWeekly(Document doc) {

        Elements tables = doc.body().getElementsByTag("table");

        StringBuilder output = new StringBuilder();

        for (Element e : tables) {
            if ("width: 556px;".equalsIgnoreCase(e.attr("style"))) {
                output.append(e.text()).append(SystemUtils.LINE_SEPARATOR);
            }
        }

        return output.toString();
    }

    private String cleanFatWrap(Document doc) {

        Elements paras = doc.body().getElementsByTag("p");

        StringBuilder output = new StringBuilder();

        for (Element e : paras) {
            output.append(e.text()).append(SystemUtils.LINE_SEPARATOR);
        }

        return output.toString();
    }
}
