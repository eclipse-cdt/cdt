/*******************************************************************************
 * Copyright (c) 2012, 2013 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alex Ruiz (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx.externaltool;

import java.util.ArrayList;
import java.util.List;

/**
 * Separates the arguments, stored as a single {@code String}, to pass to an external tool. It uses
 * an empty space as the delimiter and supports quoted arguments.
 *
 * @since 2.1
 */
public class ArgsSeparator {
	private static final char BACKSLASH = '\\';
	private static final char DOUBLE_QUOTE = '"';
	private static final char SINGLE_QUOTE = '\'';
	private static final char SPACE = ' ';

	private static final String[] NO_ARGS = {};

	public String[] splitArguments(String s) {
		if (s == null || s.isEmpty()) {
			return NO_ARGS;
		}
		ParserState state = ParserState.NORMAL;
		StringBuilder current = new StringBuilder();
		List<String> args = new ArrayList<>();
		boolean lastTokenInQuotes = false;
		char previous = 0;
		for (char c : s.toCharArray()) {
			switch (state) {
			case IN_SINGLE_QUOTE:
				if (previous != BACKSLASH && c == SINGLE_QUOTE) {
					lastTokenInQuotes = true;
					state = ParserState.NORMAL;
				} else {
					previous = c;
					current.append(c);
				}
				break;
			case IN_DOUBLE_QUOTE:
				if (previous != BACKSLASH && c == DOUBLE_QUOTE) {
					lastTokenInQuotes = true;
					state = ParserState.NORMAL;
				} else {
					previous = c;
					current.append(c);
				}
				break;
			default:
				switch (c) {
				case SINGLE_QUOTE:
					if (previous != BACKSLASH) {
						state = ParserState.IN_SINGLE_QUOTE;
					}
					break;
				case DOUBLE_QUOTE:
					if (previous != BACKSLASH) {
						state = ParserState.IN_DOUBLE_QUOTE;
					}
					break;
				case SPACE:
					if (lastTokenInQuotes || current.length() != 0) {
						args.add(current.toString());
						current.setLength(0);
					}
					break;
				default:
					previous = c;
					current.append(c);
				}
				lastTokenInQuotes = false;
				break;
			}
		}
		if (lastTokenInQuotes || current.length() != 0) {
			args.add(current.toString());
		}
		if (state != ParserState.NORMAL) {
			throw new IllegalArgumentException("Unbalanced quotes in " + s); //$NON-NLS-1$
		}
		return args.toArray(new String[args.size()]);
	}

	private static enum ParserState {
		NORMAL, IN_SINGLE_QUOTE, IN_DOUBLE_QUOTE;
	}
}
