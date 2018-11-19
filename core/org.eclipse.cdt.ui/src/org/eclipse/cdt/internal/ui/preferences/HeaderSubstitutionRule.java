/*******************************************************************************
 * Copyright (c) 2013 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

/**
 * Substitution rule for a single header file.
 */
class HeaderSubstitutionRule {
	private final String source;
	private final String target;
	private final boolean unconditional;

	public HeaderSubstitutionRule(String source, String target, boolean unconditional) {
		this.source = source;
		this.target = target;
		this.unconditional = unconditional;
	}

	public String getSource() {
		return source;
	}

	public String getTarget() {
		return target;
	}

	public boolean isUnconditionalSubstitution() {
		return unconditional;
	}

	/** For debugging only */
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append(source).append(' ');
		buf.append(unconditional ? '=' : '-').append('>');
		buf.append(' ').append(target);
		return buf.toString();
	}
}
