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
package org.eclipse.cdt.core.dom.ast;

/**
 * Represents a sequence of code tokens.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 5.4
 */
public interface IASTTokenList extends IASTToken {
	/**
	 * {@code NESTED_TOKEN} describes the relationship between
	 * {@code IASTTokenList} and the nested {@code IASTToken}s.
	 */
	public static final ASTNodeProperty NESTED_TOKEN = new ASTNodeProperty(
			"IASTTokenList.NESTED_TOKEN - Nested IASTToken for IASTTokenList"); //$NON-NLS-1$

	/**
	 * Returns nested tokens.
	 */
	public IASTToken[] getTokens();

	/**
	 * Adds a nested token.
	 *
	 * @param token a token to be added to the list
	 */
	public void addToken(IASTToken token);

	/**
	 * If the list contains a single token, returns its type. Otherwise returns 0.
	 * @see org.eclipse.cdt.core.parser.IToken#getType()
	 */
	@Override
	public int getTokenType();

	/**
	 * If the list contains a single token, returns its text. Otherwise returns {@code null}.
	 * @see org.eclipse.cdt.core.parser.IToken#getCharImage()
	 */
	@Override
	public char[] getTokenCharImage();

	@Override
	public IASTTokenList copy();

	@Override
	public IASTTokenList copy(CopyStyle style);
}
