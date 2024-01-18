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

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.parser.scanner.Lexer.LexerOptions;

/**
 * Models macros used by the preprocessor
 * @since 5.0
 */
public abstract class PreprocessorMacro implements IMacroBinding {
	final private char[] fName;

	public PreprocessorMacro(char[] name) {
		fName = name;
	}

	@Override
	final public ILinkage getLinkage() {
		return Linkage.NO_LINKAGE;
	}

	@Override
	final public char[] getNameCharArray() {
		return fName;
	}

	@Override
	final public String getName() {
		return new String(fName);
	}

	@Override
	public IScope getScope() {
		return null;
	}

	@Override
	public IBinding getOwner() {
		return null;
	}

	@Override
	public boolean isFunctionStyle() {
		return false;
	}

	@Override
	public char[][] getParameterList() {
		return null;
	}

	@Override
	public char[][] getParameterPlaceholderList() {
		return null;
	}

	@Override
	public <T> T getAdapter(Class<T> clazz) {
		return null;
	}

	/**
	 * Returns {@link FunctionStyleMacro#NO_VAARGS}
	 */
	public int hasVarArgs() {
		return FunctionStyleMacro.NO_VAARGS;
	}

	@Override
	public String toString() {
		char[][] p = getParameterList();
		if (p == null) {
			return getName();
		}
		StringBuffer buf = new StringBuffer();
		buf.append(getNameCharArray());
		buf.append('(');
		for (int i = 0; i < p.length; i++) {
			if (i > 0) {
				buf.append(',');
			}
			buf.append(p[i]);
		}
		buf.append(')');
		return buf.toString();
	}

	public abstract TokenList getTokens(MacroDefinitionParser parser, LexerOptions lexOptions, MacroExpander expander);
}
