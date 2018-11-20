/*******************************************************************************
 * Copyright (c) 2008, 2013 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.utils;

import java.util.ArrayList;

import org.eclipse.osgi.service.environment.Constants;

/**
 * Utilities to work with command line, parse arguments, etc.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @since 5.1
 */
public class CommandLineUtil {

	private static boolean isWindows() {
		boolean osWin;
		try {
			osWin = Platform.getOS().equals(Constants.OS_WIN32);
		} catch (Exception e) {
			osWin = false;
		}
		return osWin;
	}

	public static String[] argumentsToArray(String line) {
		if (isWindows()) {
			return argumentsToArrayWindowsStyle(line);
		} else {
			return argumentsToArrayUnixStyle(line);
		}
	}

	/**
	 * Parsing arguments in a shell style. i.e.
	 *
	 * <pre>
	 * ["a b c" d] -> [[a b c],[d]]
	 * [a   d] -> [[a],[d]]
	 * ['"quoted"'] -> [["quoted"]]
	 * [\\ \" \a] -> [[\],["],[a]]
	 * ["str\\str\a"] -> [[str\str\a]]
	 * </pre>
	 *
	 * @param line
	 * @return array of arguments, or empty array if line is null or empty
	 */
	public static String[] argumentsToArrayUnixStyle(String line) {
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
		ArrayList<String> aList = new ArrayList<>();
		StringBuilder buffer = new StringBuilder();
		int state = INITIAL;
		for (int i = 0; i < array.length; i++) {
			char c = array[i];

			switch (state) {
			case IN_ARG:
				// fall through
			case INITIAL:
				if (Character.isWhitespace(c)) {
					if (state == INITIAL)
						break; // ignore extra spaces
					// add argument
					state = INITIAL;
					String arg = buffer.toString();
					buffer = new StringBuilder();
					aList.add(arg);
				} else {
					switch (c) {
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
					buffer.append("\n"); //$NON-NLS-1$
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

		if (state != INITIAL) { // this allow to process empty string as an
								// argument
			aList.add(buffer.toString());
		}
		return aList.toArray(new String[aList.size()]);
	}

	/**
	 * Parsing arguments in a cmd style. i.e.
	 *
	 * <pre>
	 * ["a b c" d] -> [[a b c],[d]]
	 * [a   d] -> [[a],[d]]
	 * ['"quoted"'] -> [['quoted']]
	 * [\\ \" \a] -> [[\\],["],[\a]]
	 * ["str\\str\a"] -> [[str\\str\a]]
	 * </pre>
	 *
	 * @param line
	 * @return array of arguments, or empty array if line is null or empty
	 */
	public static String[] argumentsToArrayWindowsStyle(String line) {
		final int INITIAL = 0;
		final int IN_DOUBLE_QUOTES = 1;
		final int IN_DOUBLE_QUOTES_ESCAPED = 2;
		final int ESCAPED = 3;
		final int IN_ARG = 5;

		if (line == null) {
			line = ""; //$NON-NLS-1$
		}

		char[] array = line.trim().toCharArray();
		ArrayList<String> aList = new ArrayList<>();
		StringBuilder buffer = new StringBuilder();
		int state = INITIAL;
		for (int i = 0; i < array.length; i++) {
			char c = array[i];

			switch (state) {
			case IN_ARG:
				// fall through
			case INITIAL:
				if (Character.isWhitespace(c)) {
					if (state == INITIAL)
						break; // ignore extra spaces
					// add argument
					state = INITIAL;
					String arg = buffer.toString();
					buffer = new StringBuilder();
					aList.add(arg);
				} else {
					switch (c) {
					case '\\':
						state = ESCAPED;
						break;
					case '\"':
						state = IN_DOUBLE_QUOTES;
						break;
					default:
						state = IN_ARG;
						buffer.append(c);
						break;
					}
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
			case IN_DOUBLE_QUOTES_ESCAPED:
				switch (c) {
				case '\"':
					buffer.append(c);
					break;
				default:
					buffer.append('\\');
					buffer.append(c);
					break;
				}
				state = IN_DOUBLE_QUOTES;
				break;
			case ESCAPED:
				state = IN_ARG;
				switch (c) {
				case ' ':
				case '\"':
					buffer.append(c);
					break;
				default:
					buffer.append('\\');
					buffer.append(c);
					break;
				}
				break;
			}
		}

		if (state != INITIAL) { // this allow to process empty string as an
								// argument
			aList.add(buffer.toString());
		}
		return aList.toArray(new String[aList.size()]);
	}

	/**
	 * Converts argument array to a string suitable for passing to Bash like:
	 *
	 * This process reverses {@link #argumentsToArray(String)}, but does not
	 * restore the exact same results.
	 *
	 * @param args
	 *            the arguments to convert and escape
	 * @param encodeNewline
	 *            <code>true</code> if newline (<code>\r</code> or
	 *            <code>\n</code>) should be encoded
	 *
	 * @return args suitable for passing to some process that decodes the string
	 *         into an argument array
	 * @since 6.2
	 */
	public static String argumentsToString(String[] args, boolean encodeNewline) {
		if (isWindows()) {
			return argumentsToStringWindowsCreateProcess(args, encodeNewline);
		} else {
			// XXX: Bug 507568: We are currently using incorrect assumption that
			// shell is always bash. AFAIK this is only problematic when
			// encoding newlines
			return argumentsToStringBash(args, encodeNewline);
		}

	}

	/**
	 * Converts argument array to a string suitable for passing to Bash like:
	 *
	 * <pre>
	 * /bin/bash -c &lt;args&gt;
	 * </pre>
	 *
	 * In this case the arguments array passed to exec or equivalent will be:
	 *
	 * <pre>
	 * argv[0] = "/bin/bash"
	 * argv[1] = "-c"
	 * argv[2] = argumentsToStringBashStyle(argumentsAsArray)
	 * </pre>
	 *
	 * Replace and concatenate all occurrences of:
	 * <ul>
	 * <li><code>'</code> with <code>"'"</code>
	 * <p>
	 * (as <code>'</code> is used to surround everything else it has to be
	 * quoted or escaped)</li>
	 * <li>newline character, if encoded, with <code>$'\n'</code>
	 * <p>
	 * (<code>\n</code> is treated literally within quotes or as just 'n'
	 * otherwise, whilst supplying the newline character literally ends the
	 * command)</li>
	 * <li>Anything in between and around these occurrences is surrounded by
	 * single quotes.
	 * <p>
	 * (to prevent bash from carrying out substitutions or running arbitrary
	 * code with backticks or <code>$()</code>)</li>
	 * <ul>
	 *
	 * @param args
	 *            the arguments to convert and escape
	 * @param encodeNewline
	 *            <code>true</code> if newline (<code>\r</code> or
	 *            <code>\n</code>) should be encoded
	 * @return args suitable for passing as single argument to bash
	 * @since 6.2
	 */
	public static String argumentsToStringBash(String[] args, boolean encodeNewline) {
		StringBuilder builder = new StringBuilder();

		for (String arg : args) {
			if (builder.length() > 0) {
				builder.append(' ');
			}

			builder.append('\'');
			for (int j = 0; j < arg.length(); j++) {
				char c = arg.charAt(j);
				if (c == '\'') {
					builder.append("'\"'\"'"); //$NON-NLS-1$
				} else if (c == '\r' && encodeNewline) {
					builder.append("'$'\\r''"); //$NON-NLS-1$
				} else if (c == '\n' && encodeNewline) {
					builder.append("'$'\\n''"); //$NON-NLS-1$
				} else {
					builder.append(c);
				}
			}
			builder.append('\'');
		}

		return builder.toString();
	}

	/**
	 * Converts argument array to a string suitable for passing to Windows
	 * CreateProcess
	 *
	 * @param args
	 *            the arguments to convert and escape
	 * @param encodeNewline
	 *            <code>true</code> if newline (<code>\r</code> or
	 *            <code>\n</code>) should be encoded
	 * @return args suitable for passing as single argument to CreateProcess on
	 *         Windows
	 * @since 6.2
	 */
	public static String argumentsToStringWindowsCreateProcess(String[] args, boolean encodeNewline) {
		StringBuilder builder = new StringBuilder();

		for (String arg : args) {
			if (builder.length() > 0) {
				builder.append(' ');
			}

			builder.append('"');
			for (int j = 0; j < arg.length(); j++) {
				/*
				 * backslashes are special if and only if they are followed by a
				 * double-quote (") therefore doubling them depends on what is
				 * next
				 */
				int numBackslashes = 0;
				for (; j < arg.length() && arg.charAt(j) == '\\'; j++) {
					numBackslashes++;
				}
				if (j == arg.length()) {
					appendNBackslashes(builder, numBackslashes * 2);
				} else if (arg.charAt(j) == '"') {
					appendNBackslashes(builder, numBackslashes * 2);
					builder.append('"');
				} else if ((arg.charAt(j) == '\n' || arg.charAt(j) == '\r') && encodeNewline) {
					builder.append(' ');
				} else {
					/*
					 * this really is numBackslashes (no missing * 2), that is
					 * because next character is not a double-quote (")
					 */
					appendNBackslashes(builder, numBackslashes);
					builder.append(arg.charAt(j));
				}
			}
			builder.append('"');
		}

		return builder.toString();
	}

	private static void appendNBackslashes(StringBuilder builder, int numBackslashes) {
		for (int i = 0; i < numBackslashes; i++) {
			builder.append('\\');
		}
	}
}
