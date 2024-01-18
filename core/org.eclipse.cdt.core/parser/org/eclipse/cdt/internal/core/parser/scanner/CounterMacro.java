/*******************************************************************************
 * Copyright (c) 2007, 2020 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Alexander Fedorov (ArSysOp) - Bug 561992
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner;

import org.eclipse.cdt.core.parser.IToken;

public final class CounterMacro extends DynamicMacro {
	private static final char[] ZERO = { '0' };

	private long fValue;

	public CounterMacro(char[] name) {
		super(name);
	}

	@Override
	public Token execute(MacroExpander expander) {
		return new TokenWithImage(IToken.tINTEGER, null, 0, 0, String.valueOf(fValue++).toCharArray());
	}

	@Override
	public char[] getExpansionImage() {
		return ZERO;
	}
}
