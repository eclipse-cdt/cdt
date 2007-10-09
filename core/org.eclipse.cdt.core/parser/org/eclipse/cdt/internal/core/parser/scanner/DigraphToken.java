/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.parser.scanner;

class DigraphToken extends Token {
	public DigraphToken(int kind, int offset, int endOffset) {
		super(kind, offset, endOffset);
	}

	public char[] getTokenImage() {
		return TokenUtil.getDigraphImage(getType());
	}
}
