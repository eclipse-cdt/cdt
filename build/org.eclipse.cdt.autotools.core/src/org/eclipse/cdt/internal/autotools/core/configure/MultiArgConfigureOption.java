/*******************************************************************************
 * Copyright (c) 2009, 2016 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.core.configure;

import java.util.ArrayList;
import java.util.List;

public class MultiArgConfigureOption extends AbstractConfigurationOption {

	private String value;
	private List<String> userArgs;
	private boolean isDirty;

	public MultiArgConfigureOption(String name, AutotoolsConfiguration cfg) {
		super(name, cfg);
		this.value = ""; //$NON-NLS-1$
	}

	public MultiArgConfigureOption(String name, String msgName, AutotoolsConfiguration cfg) {
		super(name, msgName, cfg);
		this.value = ""; //$NON-NLS-1$
	}

	private MultiArgConfigureOption(String name, AutotoolsConfiguration cfg, String value) {
		super(name, cfg);
		this.value = value;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public void setValue(String newValue) {
		if (!newValue.equals(value)) {
			cfg.setDirty(true);
			isDirty = true;
			value = newValue;
		}
	}

	@Override
	public boolean isParmSet() {
		return value.length() > 0;
	}

	@Override
	public boolean isMultiArg() {
		return true;
	}

	@Override
	public String getParameter() {
		return value;
	}

	@Override
	public List<String> getParameters() {
		// May be multiple user-specified options in which case we
		// need to split them up into individual options
		if (!isDirty && userArgs != null)
			return userArgs;
		// Otherwise, we need to calculate userArgs
		userArgs = new ArrayList<>();
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

	@Override
	public IConfigureOption copy(AutotoolsConfiguration config) {
		return new MultiArgConfigureOption(name, config, value);
	}

	@Override
	public int getType() {
		return MULTIARG;
	}
}
