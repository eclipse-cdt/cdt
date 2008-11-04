package org.eclipse.cdt.utils;

import java.util.ArrayList;

/**
 * Utils to work with command line, parse arguments, etc.
 * @since 5.1
 */
public class CommandLineUtil {

	/**
	 * Parsing arguments in a shell style.
	 * i.e.
	 * <code>
	 * ["a b c" d] -> [[a b c],[d]]
	 * [a   d] -> [[a],[d]]
	 * ['"quoted"'] -> [["quoted"]]
	 * [\\ \" \a] -> [[\],["],[a]]
	 * ["str\\str\a"] -> [[str\str\a]]
	 * </code>
	 * @param line
	 * @return array of arguments, or empty array if line is null or empty
	 */
	public static String[] argumentsToArray(String line) {
		final int INITIAL = 0;
		final int IN_DOUBLE_QUOTES = 1;
		final int IN_DOUBLE_QUOTES_ESCAPED = 2;
		final int ESCAPED = 3;
		final int IN_SINGLE_QUOTES = 4;
		final int IN_ARG = 5;

		if (line == null) {
			line = ""; //$NON-NLS-1$
		}

		char[] array = line.trim().toCharArray();
		ArrayList<String> aList = new ArrayList<String>();
		StringBuilder buffer = new StringBuilder();
		int state = INITIAL;
		for (int i = 0; i < array.length; i++) {
			char c = array[i];
			switch (state) {
				case IN_ARG:
					// fall through
				case INITIAL:
					switch (c) {
						case ' ':
							if (state == INITIAL) break; // ignore extra spaces
							// add argument
							state = INITIAL;
							String arg = buffer.toString();
							buffer = new StringBuilder();
							aList.add(arg);
							break;
						case '\\':
							state = ESCAPED;
							break;
						case '\'':
							state = IN_SINGLE_QUOTES;
							break;
						case '\"':
							state = IN_DOUBLE_QUOTES;
							break;
						default:
							state = IN_ARG;
							buffer.append(c);
							break;
					}
					break;
				case IN_DOUBLE_QUOTES:
					switch (c) {
						case '\\':
							state = IN_DOUBLE_QUOTES_ESCAPED;
							break;
						case '\"':
							state = IN_ARG;
							break;
						default:
							buffer.append(c);
							break;
					}
					break;
				case IN_SINGLE_QUOTES:
					switch (c) {
						case '\'':
							state = IN_ARG;
							break;
						default:
							buffer.append(c);
							break;
					}
					break;
				case IN_DOUBLE_QUOTES_ESCAPED:
					switch (c) {
						case '\"':
						case '\\':
							buffer.append(c);
							break;
						case 'n':
							buffer.append('\n');
							break;
						default:
							buffer.append('\\');
							buffer.append(c);
							break;
					}
					state = IN_DOUBLE_QUOTES;
					break;
				case ESCAPED:
					buffer.append(c);
					state = IN_ARG;
					break;
			}
		}

		if (state != INITIAL) { // this allow to process empty string as an argument
			aList.add(buffer.toString());
		}
		return aList.toArray(new String[aList.size()]);
	}

}
