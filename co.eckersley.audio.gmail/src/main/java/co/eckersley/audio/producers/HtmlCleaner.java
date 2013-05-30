package co.eckersley.audio.producers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public abstract class HtmlCleaner extends TextCleaner {

    private final String START_MARKER_1 = "<!DOCTYPE html PUBLIC";
    private final String START_MARKER_2 = "<html";
    private final String END_MARKER_1 = "</html>";
    
    public abstract String clean(String from, String subject, Document doc);

    public String clean(String from, String subject, String input) {

        int start = 0;
        int end = input.length();

        // Strip Start blurb
        if ((start = input.indexOf(START_MARKER_1)) >= 0) {
            input = stripStart(start, input);
        } else if ((start = input.indexOf(START_MARKER_2)) >= 0) {
            input = stripStart(start, input);
        } else {
            logger.error("Couldn't find start marker!");
            return null;
        }

        // Tail email
        if ((end = input.indexOf(END_MARKER_1)) >= 0) {
            input = stripEnd(end, input);
        } else {
            logger.error("Couldn't find end marker!");
            return null;
        }

        return clean(from, subject, Jsoup.parse(input));
    }
}
