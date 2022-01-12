/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
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
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTToken;
import org.eclipse.cdt.core.dom.ast.IASTTokenList;
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
		ASTTokenList copy = new ASTTokenList();
		for (IASTToken token : tokens) {
			if (token == null)
				break;
			copy.addToken(token.copy(style));
		}
		return copy(copy, style);
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
	public int getTokenType() {
		IASTToken[] tok = getTokens();
		return tok != null && tok.length == 1 ? tok[0].getTokenType() : 0;
	}

	@Override
	public char[] getTokenCharImage() {
		IASTToken[] tok = getTokens();
		return tok != null && tok.length == 1 ? tok[0].getTokenCharImage() : null;
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitTokens) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}

		for (IASTToken token : tokens) {
			if (token == null)
				break;
			if (!token.accept(action))
				return false;
		}

		if (action.shouldVisitTokens && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;

		return true;
	}
}
