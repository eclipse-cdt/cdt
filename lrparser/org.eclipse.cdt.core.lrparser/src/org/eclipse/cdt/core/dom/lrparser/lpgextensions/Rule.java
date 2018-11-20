/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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
 *********************************************************************************/

package org.eclipse.cdt.core.dom.lrparser.lpgextensions;

/**
 * Provides information about a reduction rule that a parser has
 * encountered.
 */
class Rule<RULE_DATA> {
	private int ruleNumber;
	private int startTokenOffset;
	private int endTokenOffset;
	private RULE_DATA data;

	public Rule(int ruleNumber, int startTokenOffset, int endTokenOffset) {
		this.ruleNumber = ruleNumber;
		this.startTokenOffset = startTokenOffset;
		this.endTokenOffset = endTokenOffset;
	}

	public int getRuleNumber() {
		return ruleNumber;
	}

	public int getStartTokenOffset() {
		return startTokenOffset;
	}

	public int getEndTokenOffset() {
		return endTokenOffset;
	}

	@Override
	public String toString() {
		return String.valueOf(ruleNumber);
	}

	public RULE_DATA getData() {
		return data;
	}

	public void setData(RULE_DATA data) {
		this.data = data;
	}
}
