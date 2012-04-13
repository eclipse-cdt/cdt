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
package org.eclipse.cdt.core.dom.ast;

import org.eclipse.cdt.core.parser.IToken;

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
	 * Returns the token.
	 */
	public IToken getToken();

	@Override
	public IASTToken copy();

	@Override
	public IASTToken copy(CopyStyle style);
}
