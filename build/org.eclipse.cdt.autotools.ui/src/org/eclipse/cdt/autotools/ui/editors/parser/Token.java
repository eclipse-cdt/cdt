/*******************************************************************************
 * Copyright (c) 2008 Nokia Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ed Swartz (Nokia) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.autotools.ui.editors.parser;

import org.eclipse.jface.text.IDocument;

/**
 * A single token parsed from an autotools-style file.  This represents m4 and sh
 * tokens.  Punctuation characters shared by both are not in a namespace.
 * 
 * @author eswartz
 *
 */
public class Token implements ITokenConstants {
	/** Type: 
	 * @see ITokenConstants
	 */
	final int type;
	/**
	 * Text of token, possibly interpreted or reduced from original characters
	 */
	final String text;
	/**
	 * Offset of token before interpretation
	 */
	final int offset;
	/**
	 * Length of token before interpretation
	 */
	final int length;
	/**
	 * The document providing the text
	 */
	final IDocument document;

	public Token(int type, String text, IDocument document, int offset, int length) {
		this.type = type;
		this.text = text;
		this.document = document;
		this.offset = offset;
		this.length = length;
	}
	
	public String toString() {
		return text;
	}

	public int getType() {
		return type;
	}

	public String getText() {
		return text;
	}

	public IDocument getDocument() {
		return document;
	}

	public int getOffset() {
		return offset;
	}
	
	public int getLength() {
		return length;
	}

	public boolean followsSpace() {
		char[] text = document.get().toCharArray();
		if (offset == 0)
			return false;
		return (" \t\r\n\f".indexOf(text[offset - 1]) >= 0);
	}
	
}
