package co.eckersley.audio.producers;

import org.jsoup.nodes.Document;

public class DailyWealthCleaner extends HtmlCleaner {

    private final String START_ADVERTISEMENT_MARKER = "----------Advertisement---------";
    private final String END_ADVERTISEMENT_MARKER = "---------------------------------";

    private final String START_MARKER = "To unsubscribe from DailyWealth, click the following link or copy and paste it into your browser: click here to unsubscribe ";
    private final String END_MARKER_1 = "You are receiving this message as part of a subscribers-only e-mail service";

    private final String END_MARKER_2 = "Home | About Us | Resources";

    public String clean(String from, String subject, Document doc) {

        int start, end = 0;

        String output = doc.body().text();

        // Strip DailyWealth Tail blurb
        end = output.indexOf(END_MARKER_1);
        if (output.contains(END_MARKER_1)) {
            output = stripEnd(end, output);
        }

        // Strip DailyWealth Adverts
        start = output.indexOf(START_ADVERTISEMENT_MARKER);
        end = output.indexOf(END_ADVERTISEMENT_MARKER, start) + END_ADVERTISEMENT_MARKER.length() + 1;
        if (start >= 0 && end >= 0) {
            output = strip(start, end, output);
        }

        // Strip DailyWealth Start blurb
        start = output.indexOf(START_MARKER);
        if (start >= 0) {
            output = stripStart(start + START_MARKER.length(), output);
        }

        end = output.indexOf(END_MARKER_2);
        if (output.contains(END_MARKER_2)) {
            output = stripEnd(end, output);
        }

        return output;
    }
}
