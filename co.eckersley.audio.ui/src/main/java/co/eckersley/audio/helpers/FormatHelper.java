package co.eckersley.audio.helpers;

import java.text.MessageFormat;

public class FormatHelper {

	public static String f(String msg, Object ... params) {
		return MessageFormat.format(msg, params);
	}

}
