/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.testplugin.util;

import java.util.Stack;

/**
 * @author Peter Graves
 *
 * This utility class maintains a list of strings, and as a tests finds strings
 * in a structure/list, it will maintain a list of unfound/extra strings.
 */
public class ExpectedStrings {
	public String[] expStrings;
	private boolean[] foundStrings;
	private Stack<String> extraStrings; // A stack of the unexpected strings we received
	private boolean extra;

	/**
	 * Constructor for ExpectedStrings.
	 */
	public ExpectedStrings() {
	}

	/**
	 * Constructor for ExpectedStrings that accepts a list of strings that we expect to get.
	 */
	public ExpectedStrings(String[] values) {
		expStrings = new String[values.length];
		for (int x = 0; x < values.length; x++) {
			expStrings[x] = values[x];
		}
		foundStrings = new boolean[values.length];
		for (int x = 0; x < values.length; x++) {
			foundStrings[x] = false;
		}
		extraStrings = new Stack<>();
		extra = false;
	}

	public int foundString(String current) {
		for (int x = 0; x < expStrings.length; x++) {
			if (current.equals(expStrings[x])) {
				foundStrings[x] = true;
				return 0;
			}
		}

		// If we arrive here, the strings was not found, so this is and extra string.
		extraStrings.push(current);
		extra = true;
		return 1;
	}

	public int getNum(String name) {
		for (int x = 0; x < expStrings.length; x++) {
			if (name.equals(expStrings[x]))
				return x;
		}
		return -1;
	}

	public boolean gotAll() {
		for (int x = 0; x < expStrings.length; x++) {
			if (!foundStrings[x])
				return false;
		}
		return true;
	}

	public boolean gotExtra() {
		return extra;
	}

	public String getMissingString() {
		StringBuilder missing = new StringBuilder("Missing elements: ");
		for (int x = 0; x < expStrings.length; x++) {
			if (!foundStrings[x])
				missing.append(expStrings[x]).append(" ");
		}
		return missing.toString();
	}

	public String getExtraString() {
		StringBuilder extra = new StringBuilder("Extra elements: ");
		while (!extraStrings.empty()) {
			extra.append(extraStrings.pop()).append(" ");
		}
		return extra.toString();
	}
}
