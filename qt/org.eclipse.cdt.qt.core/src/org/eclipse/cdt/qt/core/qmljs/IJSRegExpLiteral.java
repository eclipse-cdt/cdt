/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.qt.core.qmljs;

/**
 * A JavaScript regular expression literal from the
 * <a href="https://github.com/estree/estree/blob/master/spec.md#regexpliteral">ESTree Specification</a>
 */
public interface IJSRegExpLiteral extends IJSLiteral {
	/**
	 * A JavaScript regular expression that holds a pattern and a set of flags. Both are represented as plain Strings.
	 */
	public static class JSRegExp {
		private final String pattern;
		private final String flags;

		public JSRegExp(String pattern, String flags) {
			this.pattern = pattern;
			this.flags = flags;
		}

		public String getPattern() {
			return pattern;
		}

		public String getFlags() {
			return flags;
		}
	}

	public JSRegExp getRegex();
}
