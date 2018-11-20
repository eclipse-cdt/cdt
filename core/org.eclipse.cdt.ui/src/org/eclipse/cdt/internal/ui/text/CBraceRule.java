/*******************************************************************************
 * Copyright (c) 2005, 2008 QNX Software Systems and others.
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
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import org.eclipse.jface.text.rules.IToken;

/**
 * Braces rule.
 *
 * @author P.Tomaszewski
 */
public class CBraceRule extends SingleCharRule {

	/**
	 * Creates new rule.
	 * @param token Style token.
	 */
	public CBraceRule(IToken token) {
		super(token);
	}

	/**
	 * @see org.eclipse.cdt.internal.ui.text.SingleCharRule#isRuleChar(int)
	 */
	@Override
	protected boolean isRuleChar(int ch) {
		return ch == '{' || ch == '}' || ch == '[' || ch == ']' || ch == '(' || ch == ')';
	}

}
