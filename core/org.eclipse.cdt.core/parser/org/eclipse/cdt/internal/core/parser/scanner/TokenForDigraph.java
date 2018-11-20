/*******************************************************************************
 * Copyright (c) 2007, 2013 Wind River Systems, Inc. and others.
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
