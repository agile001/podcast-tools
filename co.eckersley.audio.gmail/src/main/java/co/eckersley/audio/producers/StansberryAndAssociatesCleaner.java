package co.eckersley.audio.producers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class StansberryAndAssociatesCleaner extends TextCleaner {

	private final String HTML_MARKER_1 = "<!DOCTYPE html PUBLIC";
	private final String HTML_MARKER_2 = "<html>";

	private final String START_ADVERTISEMENT_MARKER = "----------Advertisement---------";
	private final String END_ADVERTISEMENT_MARKER = "---------------------------------";
	
	private final String START_MARKER = "<!DOCTYPE html PUBLIC";
	
	private final String END_MARKER_1 = "You are receiving this message as part of a subscribers-only e-mail service";
	private final String END_MARKER_2 = "Home | About Us | Resources";
    private final String END_MARKER_3 = "Stansberry & Associates Top 10 Open Recommendations";
	
	public String clean(String from, String subject, String input) {

		int start = 0;
		int end = input.length();
		
		// FIXME (DE) Clean this up and OO this class. Hacked for now.

		// Strip to start of html
		start = input.indexOf(HTML_MARKER_1);
		if (start >= 0) {
			input = stripStart(start, input);
		} else if (input.contains(HTML_MARKER_2)) {
			start = input.indexOf(HTML_MARKER_2);
			input = stripStart(start, input);
		} else {
			logger.warn("Couldn't find start marker!");
		}
		
		// Cleaning the html text
		Document doc = Jsoup.parse(input);
		
		String output = doc.body().text();

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

        if ((end = output.indexOf(END_MARKER_3)) > 0) {
            output = stripEnd(end, output);
        } else if ((end = output.indexOf(END_MARKER_1)) > 0) {
			output = stripEnd(end, output);
		} else if ((end = output.indexOf(END_MARKER_2)) > 0) {
			output = stripEnd(end, output);
		} else {
			logger.warn("Couldn't find end marker!");
		}

		return output;
	}
}
