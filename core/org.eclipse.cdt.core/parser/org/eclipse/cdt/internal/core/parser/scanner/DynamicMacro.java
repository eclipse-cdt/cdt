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

import org.eclipse.cdt.internal.core.parser.scanner.Lexer.LexerOptions;

public abstract class DynamicMacro extends PreprocessorMacro {

	public DynamicMacro(char[] name) {
		super(name);
	}

	@Override
	public final char[] getExpansion() {
		return getExpansionImage();
	}

	public abstract Token execute(MacroExpander expander);

	@Override
	public TokenList getTokens(MacroDefinitionParser mdp, LexerOptions lexOptions, MacroExpander expander) {
		TokenList result = new TokenList();
		result.append(execute(expander));
		return result;
	}

	final protected void append(StringBuilder buffer, int value) {
		if (value < 10)
			buffer.append("0"); //$NON-NLS-1$
		buffer.append(value);
	}

	@Override
	public final boolean isDynamic() {
		return true;
	}
}
