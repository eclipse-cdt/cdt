/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.formatter.scanner;

import java.io.CharArrayReader;
import java.io.Reader;

import org.eclipse.cdt.internal.core.CharOperation;

/**
 * A scanner operating on a character array and allowing to reposition the scanner.
 *
 * @since 4.0
 */
public class Scanner extends SimpleScanner {

	public char[] source;
	public int eofPosition;
	public int startPosition;

    public Scanner() {
    	setReuseToken(true);
    	setSplitPreprocessor(false);
    }

	/*
	 * @see org.eclipse.cdt.internal.formatter.scanner.SimpleScanner#init(java.io.Reader, java.lang.String)
	 */
	@Override
	protected void init(Reader reader, String filename) {
		// not allowed
		throw new UnsupportedOperationException();
	}

	/**
	 * Set the source text as character array.
	 * 
	 * @param source  the source text
	 */
	public void setSource(char[] source) {
		this.source= source;
		fContext= new ScannerContext().initialize(new CharArrayReader(source));
		startPosition= -1;
		eofPosition= source.length;
	}

	/**
	 * Reset scanner to given inclusive start and end offsets
	 * @param start  inclusive start offset
	 * @param end  inclusive end offset
	 */
	public void resetTo(int start, int end) {
		Reader reader;
		if (end >= source.length - 1) {
			reader= new CharArrayReader(source);
		} else {
			reader= new CharArrayReader(source, 0, Math.min(source.length, end+1));
		}
		fContext= new ScannerContext().initialize(reader, start);
		startPosition= start;
		if (source != null && source.length < end) {
			eofPosition = source.length;
		} else {
			eofPosition = end < Integer.MAX_VALUE ? end + 1 : end;
		}
	}

	/**
	 * Get the start offset of the current token.
	 * @return the start offset of the current token
	 */
	public int getCurrentTokenStartPosition() {
		return fCurrentToken.offset;
	}

	/**
	 * Get the inclusive end offset of the current token.
	 * @return the inclusive end offset of the current token
	 */
	public int getCurrentTokenEndPosition() {
		return getCurrentPosition() - 1;
	}

	/**
	 * Get the current scanner offset.
	 * @return the current scanner offset
	 */
	public int getCurrentPosition() {
		return fContext.getOffset() - fContext.undoStackSize();
	}

	/**
	 * Get the next character.
	 * @return the next character
	 */
	public int getNextChar() {
		return getChar();
	}

	/**
	 * Move to next character iff it is equal to the given expected character.
	 * If the characters do not match, the scanner does not move forward.
	 * 
	 * @param c the expected character
	 * @return <code>true</code> if the next character was the expected character
	 */
	public boolean getNextChar(char c) {
		if (c == getChar()) {
			return true;
		}
		ungetChar(c);
		return false;
	}

	/**
	 * Set current scanner offset to given offset.
	 * 
	 * @param nextCharacterStart  the desired scanner offset
	 */
	public void setCurrentPosition(int nextCharacterStart) {
		int currentPos= getCurrentPosition();
		int diff= currentPos - nextCharacterStart;
		if (diff < 0) {
			do {
				getChar();
				++diff;
			} while(diff < 0);
		} else if (diff == 0) {
			// no-op
		} else if (diff > fTokenBuffer.length()) {
			resetTo(nextCharacterStart, source.length - 1);
		} else /* if (diff <= fTokenBuffer.length()) */ {
			while (diff > 0) {
				if (fTokenBuffer.length() > 0) {
					ungetChar(fTokenBuffer.charAt(fTokenBuffer.length() - 1));
				}
				--diff;
			}
		}			
	}

	/**
	 * Get the text of the current token as a character array.
	 * @return the token text
	 */
	public char[] getCurrentTokenSource() {
		return fCurrentToken.getText().toCharArray();
	}

	/**
	 * Get the next token as token type constant.
	 * 
	 * @return the next token type
	 */
	public int getNextToken() {
		Token token= nextToken();
		if (token == null) {
			return -1;
		}
		return token.type;
	}

	/**
	 * For debugging purposes.
	 */
	@Override
	public String toString() {
		if (this.startPosition == this.source.length)
			return "EOF\n\n" + new String(this.source); //$NON-NLS-1$
		if (this.getCurrentPosition() > this.source.length)
			return "behind the EOF\n\n" + new String(this.source); //$NON-NLS-1$

		char front[] = new char[this.startPosition];
		System.arraycopy(this.source, 0, front, 0, this.startPosition);

		int middleLength = (this.getCurrentPosition() - 1) - this.startPosition + 1;
		char middle[];
		if (middleLength > -1) {
			middle = new char[middleLength];
			System.arraycopy(
				this.source, 
				this.startPosition, 
				middle, 
				0, 
				middleLength);
		} else {
			middle = CharOperation.NO_CHAR;
		}
		
		char end[] = new char[this.source.length - (this.getCurrentPosition() - 1)];
		System.arraycopy(
			this.source, 
			(this.getCurrentPosition() - 1) + 1, 
			end, 
			0, 
			this.source.length - (this.getCurrentPosition() - 1) - 1);
		
		return new String(front)
			+ "\n===============================\nStarts here -->" //$NON-NLS-1$
			+ new String(middle)
			+ "<-- Ends here\n===============================\n" //$NON-NLS-1$
			+ new String(end); 
	}
}
