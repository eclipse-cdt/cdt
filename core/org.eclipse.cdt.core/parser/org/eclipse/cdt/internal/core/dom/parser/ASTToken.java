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
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.internal.core.parser.scanner.Token;

/**
 * Base class for C and C++ attributes.
 */
public class ASTToken extends ASTNode implements IASTToken {
    private final IToken token;

	public ASTToken(IToken token) {
		this.token = token;
	}

	@Override
	public ASTToken copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public ASTToken copy(CopyStyle style) {
		Token tokenCopy = ((Token) token).clone();
		tokenCopy.setNext(null);
		return super.copy(new ASTToken(tokenCopy), style);
	}

	@Override
	public IToken getToken() {
		return token;
	}
}
