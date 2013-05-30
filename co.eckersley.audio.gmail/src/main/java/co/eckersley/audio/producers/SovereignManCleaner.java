package co.eckersley.audio.producers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class SovereignManCleaner extends TextCleaner {

	private final String START_ADVERTISEMENT_MARKER = "----------Advertisement---------";
	private final String END_ADVERTISEMENT_MARKER = "---------------------------------";
	
	private final String START_MARKER_0 = "Having trouble viewing this email? Read it on our website.";
	private final String START_MARKER_1 = "If you are having trouble viewing this email, or you'd like to share this article with your friends, click here";
	private final String END_MARKER = "Until tomorrow, Simon Black Senior Editor, SovereignMan.com";
	
	public String clean(String from, String subject, String input) {

		// Cleaning the html text
		Document doc = Jsoup.parse(input);
		
		String output = doc.body().text();

		int start = 0;
		int end = input.length();
		
		// FIXME (DE) Clean this up and OO this class. Hacked for now.

		// Strip Start blurb
		start = output.indexOf(START_MARKER_0);
		if (start >= 0) {
			output = stripStart(start + START_MARKER_0.length(), output);
		}
		start = output.indexOf(START_MARKER_1);
		if (start >= 0) {
			output = stripStart(start + START_MARKER_1.length(), output);
		}

		// Strip Adverts
		start = output.indexOf(START_ADVERTISEMENT_MARKER);
		end = output.indexOf(END_ADVERTISEMENT_MARKER, start) + END_ADVERTISEMENT_MARKER.length() + 1;
		if (start >= 0 && end >= 0) {
			output = strip(start, end, output);
		}
		
		// Strip DailyWealth Tail blurb
		end = output.indexOf(END_MARKER);
		if (output.contains(END_MARKER)) {
			output = stripEnd(end + END_MARKER.length(), output);
		}
		
		return output;
	}
}
