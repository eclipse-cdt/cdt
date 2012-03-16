/*******************************************************************************
 * Copyright (c) 2009 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.core.configure;

import java.util.ArrayList;


public class MultiArgConfigureOption extends AbstractConfigurationOption {

	private String value;
	private ArrayList<String> userArgs;
	private boolean isDirty;
	
	public MultiArgConfigureOption(String name, AutotoolsConfiguration cfg) {
		super(name, cfg);
		this.value = ""; // $NON-NLS-1$
	}
	
	public MultiArgConfigureOption(String name, String msgName, AutotoolsConfiguration cfg) {
		super(name, msgName, cfg);
		this.value = ""; // $NON-NLS-1$
	}
	
	private MultiArgConfigureOption(String name, AutotoolsConfiguration cfg,
			String value) {
		super(name, cfg);
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String newValue) {
		if (!newValue.equals(value)) {
			cfg.setDirty(true);
			isDirty = true;
			value = newValue;
		}
	}

	public boolean isParmSet() {
		return value.length() > 0;
	}
	
	public boolean isMultiArg() {
		return true;
	}
	
	public String getParameter() {
		return value;
	}
	
	public ArrayList<String> getParameters() {
		// May be multiple user-specified options in which case we
		// need to split them up into individual options
		if (!isDirty && userArgs != null)
			return userArgs;
		// Otherwise, we need to calculate userArgs
		userArgs = new ArrayList<String>();
		isDirty = false;
		int lastArgIndex = -1;
		int i = 0;
		while (i < value.length()) {
			char ch = value.charAt(i);
			// Skip white-space
			while (Character.isWhitespace(ch)) {
				++i;
				if (i < value.length())
					ch = value.charAt(i);
				else // Otherwise we are done
					return userArgs;
			}

			// Simplistic parser.  We break up into strings delimited
			// by blanks.  If quotes are used, we ignore blanks within.
			// If a backslash is used, we ignore the next character and
			// pass it through.
			lastArgIndex = i;
			boolean inString = false;
			while (i < value.length()) {
				ch = value.charAt(i);
				if (ch == '\\') // escape character
					++i; // skip over the next character
				else if (ch == '\"') { // double quotes
					inString = !inString;
				} else if (Character.isWhitespace(ch)) {
					if (!inString) {
						userArgs.add(value.substring(lastArgIndex, i));
						break;
					}
				}
				++i;
			}
			// Look for the case where we ran out of chars for the last
			// token.
			if (i >= value.length())
				userArgs.add(value.substring(lastArgIndex));
			++i;
		}
		return userArgs;
	}

	public IConfigureOption copy(AutotoolsConfiguration config) {
		return new MultiArgConfigureOption(name, config, value);
	}

	public int getType() {
		return MULTIARG;
	}
}
