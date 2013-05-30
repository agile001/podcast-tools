package co.eckersley.audio.producers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TextCleaner {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	public abstract String clean(String from, String subject, String input);
	
	protected String stripStart(int start, String input) {
		return input.substring(start);
	}
	
	protected String strip(int start, int end, String input) {
		return input.substring(0, start) + input.substring(end);
	}
	
	protected String stripEnd(int end, String input) {
		return input.substring(0, end);
	}
}
