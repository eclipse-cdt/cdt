/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IASTToken;
import org.eclipse.cdt.core.dom.ast.IASTTokenList;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

/**
 * Represents a sequence of code tokens.
 */
public class ASTTokenList extends ASTNode implements IASTTokenList {
    private IASTToken[] tokens = IASTToken.EMPTY_TOKEN_ARRAY;

	public ASTTokenList() {
	}

	@Override
	public ASTTokenList copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public ASTTokenList copy(CopyStyle style) {
		ASTTokenList copy = super.copy(new ASTTokenList(), style);
		for (IASTToken token : tokens) {
			if (token == null)
				break;
			copy.addToken(token.copy(style));
		}
		return copy;
	}

	@Override
	public IASTToken[] getTokens() {
		tokens = ArrayUtil.trim(tokens);
		return tokens;
	}

	@Override
	public void addToken(IASTToken token) {
		tokens = ArrayUtil.append(tokens, token);
	}

	@Override
	public IToken getToken() {
		IASTToken[] tok = getTokens();
		return tok != null && tok.length == 1 ? tok[0].getToken() : null;
	}
}
