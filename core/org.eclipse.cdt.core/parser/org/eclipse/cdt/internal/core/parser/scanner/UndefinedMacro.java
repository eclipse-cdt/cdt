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

public final class UndefinedMacro extends PreprocessorMacro {
	public UndefinedMacro(char[] name) {
		super(name);
	}

	@Override
	public TokenList getTokens(MacroDefinitionParser parser, LexerOptions lexOptions, MacroExpander expander) {
		return null;
	}

	@Override
	public char[] getExpansion() {
		return null;
	}

	@Override
	public char[] getExpansionImage() {
		return null;
	}

	@Override
	public boolean isDynamic() {
		return false;
	}
}
