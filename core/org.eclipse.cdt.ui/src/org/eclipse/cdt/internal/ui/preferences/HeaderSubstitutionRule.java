/*******************************************************************************
 * Copyright (c) 2013 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
