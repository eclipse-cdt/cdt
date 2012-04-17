/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Ruiz (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx.externaltool;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Separates the arguments, stored as a single {@code String}, to pass to an
 * external tool. It uses
 * an empty space as the delimiter.
 * 
 * @since 2.1
 */
public class ArgsSeparator {
	private static final String SPACE = " "; //$NON-NLS-1$
	private static final String DOUBLE_QUOTE = "\""; //$NON-NLS-1$
	private static final String SINGLE_QUOTE = "\'"; //$NON-NLS-1$

	// parser states
	private static final int NORMAL = 0;
	private static final int IN_SINGLE_QUOTE = 1;
	private static final int IN_DOUBLE_QUOTE = 2;
	
	private static final String[] NO_ARGS = {};

	public String[] splitArguments(String s) {
		if (s == null || s.isEmpty()) {
			return NO_ARGS;
		}
		int state = NORMAL;
		String delimiter = DOUBLE_QUOTE + SINGLE_QUOTE + SPACE;
		StringTokenizer tokenizer = new StringTokenizer(s, delimiter, true);
		List<String> args = new ArrayList<String>();
		StringBuilder current = new StringBuilder();
		boolean lastTokenInQuotes = false;
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			switch (state) {
				case IN_SINGLE_QUOTE:
					if (SINGLE_QUOTE.equals(token)) {
						lastTokenInQuotes = true;
						state = NORMAL;
					} else {
						current.append(token);
					}
					break;
				case IN_DOUBLE_QUOTE:
					if (DOUBLE_QUOTE.equals(token)) {
						lastTokenInQuotes = true;
						state = NORMAL;
					} else {
						current.append(token);
					}
					break;
				default:
					if (SINGLE_QUOTE.equals(token)) {
						state = IN_SINGLE_QUOTE;
					} else if (DOUBLE_QUOTE.equals(token)) {
						state = IN_DOUBLE_QUOTE;
					} else if (SPACE.equals(token)) {
						if (lastTokenInQuotes || current.length() != 0) {
							args.add(current.toString());
							current = new StringBuilder();
						}
					} else {
						current.append(token);
					}
					lastTokenInQuotes = false;
					break;
			}
		}
		if (lastTokenInQuotes || current.length() != 0) {
			args.add(current.toString());
		}
		if (state == IN_SINGLE_QUOTE || state == IN_DOUBLE_QUOTE) {
			throw new IllegalArgumentException("Unbalanced quotes in " + s); //$NON-NLS-1$
		}
		return args.toArray(new String[args.size()]);
	}
}
