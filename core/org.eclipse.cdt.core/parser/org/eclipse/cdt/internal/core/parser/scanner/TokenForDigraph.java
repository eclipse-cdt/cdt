/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.parser.scanner;

/**
 * Tokens for digraphs simply have a different image.
 * @since 5.0
 */
public class TokenForDigraph extends Token {
	public TokenForDigraph(int kind, Object source, int offset, int endOffset) {
		super(kind, source, offset, endOffset);
	}

	@Override
	public char[] getCharImage() {
		return TokenUtil.getDigraphImage(getType());
	}
}
