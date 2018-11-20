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
 * Represents an arbitrary code token.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 5.4
 */
public interface IASTToken extends IASTNode {
	public static final IASTToken[] EMPTY_TOKEN_ARRAY = {};

	/**
	 * Returns the token type.
	 * @see org.eclipse.cdt.core.parser.IToken#getType()
	 */
	public int getTokenType();

	/**
	 * Returns the token text.
	 * @see org.eclipse.cdt.core.parser.IToken#getCharImage()
	 */
	public char[] getTokenCharImage();

	@Override
	public IASTToken copy();

	@Override
	public IASTToken copy(CopyStyle style);
}
