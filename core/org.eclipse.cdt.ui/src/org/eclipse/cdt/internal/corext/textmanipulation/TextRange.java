/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.textmanipulation;

import org.eclipse.jface.util.Assert;



public final class TextRange {

	/* package */ int fOffset;
	/* package */ int fLength;
	
	public static final TextRange UNDEFINED= new TextRange((TextRange)null);

	/**
	 * Creates a insert position with the given offset.
	 *
	 * @param offset the position offset, must be >= 0
	 */
	public TextRange(int offset) {
		this(offset, 0);
	}
	
	/**
	 * Creates a new range with the given offset and length.
	 *
	 * @param offset the position offset, must be >= 0
	 * @param length the position length, must be >= 0
	 */
	public TextRange(int offset, int length) {
		fOffset= offset;
		Assert.isTrue(fOffset >= 0);
		fLength= length;
		Assert.isTrue(fLength >= 0);
	}
	
	/**
	 * Constructor for the undefined text range.
	 */
	private TextRange(TextRange dummy) {
		fOffset= -1;
		fLength= -1;
	}
	
	public static TextRange createFromStartAndLength(int start, int length) {
		return new TextRange(start, length);
	}
	
	public static TextRange createFromStartAndInclusiveEnd(int start, int end) {
		return new TextRange(start, end - start + 1);
	}
	
	public static TextRange createFromStartAndExclusiveEnd(int start, int end) {
		return new TextRange(start, end - start);
	}
	
	/**
	 * Creates a new range from the given source range.
	 * 
	 * @range the source range denoting offset and length
	 */
	public TextRange(ISourceRange range) {
		this(range.getOffset(), range.getLength());
	}
	
	/**
	 * Returns the offset of this range.
	 *
	 * @return the length of this range
	 */
	public int getOffset() {
		return fOffset;
	}
	
	/**
	 * Returns the length of this range.
	 *
	 * @return the length of this range
	 */
	public int getLength() {
		return fLength;
	}
	
	/**
	 * Returns the inclusive end position of this range. That means that the end position
	 * denotes the last character of this range.
	 * 
	 * @return the inclusive end position
	 */
	public int getInclusiveEnd() {
		return fOffset + fLength - 1;
	}
	
	/**
	 * Returns the exclusive end position of this range. That means that the end position
	 * denotes the first character after this range.
	 * 
	 * @return the exclusive end position
	 */
	public int getExclusiveEnd() {
		return fOffset + fLength;
	}
	
	/**
	 * Creates a copy of this <code>TextRange</code>.
	 * 
	 * @return a copy of this <code>TextRange</code>
	 */
	public TextRange copy() {
		if (isUndefined())
			return this;
		return new TextRange(fOffset, fLength);
	}
	
	/**
	 * Returns <code>true</code> if this text range is the <code>UNDEFINED</code>
	 * text range. Otherwise <code>false</code> is returned.
	 */
	public boolean isUndefined() {
		return UNDEFINED == this;
	}
	
	/**
	 * Checks if this <code>TextRange</code> is valid. For valid text range the following
	 * expression evaluates to <code>true</code>:
	 * <pre>
	 * 	getOffset() >= 0 && getLength() >= 0
	 * </pre>
	 * 
	 * @return <code>true</code> if this text range is a valid range. Otherwise <code>
	 * 	false</code>
	 */
	public boolean isValid() {
		return fOffset >= 0 && fLength >= 0;
	}
	
	/* package */ boolean isInsertionPoint() {
		return fLength == 0;
	}
	
	/* package */ boolean equals(TextRange range) {
		return fOffset == range.fOffset && fLength == range.fLength;
	}

	/* package */ boolean isEqualInsertionPoint(TextRange range)	{
		return fLength == 0 && range.fLength == 0 && fOffset == range.fOffset;
	}

	/* package */ boolean liesBehind(TextRange range) {
		return fOffset >= range.fOffset + range.fLength;
	}

	/* package */ boolean isInsertionPointAt(int o) {
		return fOffset == o && fLength == 0;
	}
	
	/* package */ boolean covers(TextRange other) {
		if (fLength == 0) {	// an insertion point can't cover anything
			return false;
		} else if (other.fLength == 0) {
			int otherOffset= other.fOffset;
			return fOffset < otherOffset && otherOffset < fOffset + fLength;
		} else {
			int otherOffset= other.fOffset;
			return fOffset <= otherOffset && otherOffset + other.fLength <= fOffset + fLength;
		}
	}
	/* non Java-doc
	 * @see Object#toString()
	 */
	public String toString() {
		StringBuffer buffer= new StringBuffer();
		buffer.append(TextManipulationMessages.getString("TextRange.offset")); //$NON-NLS-1$
		buffer.append(fOffset);
		buffer.append(TextManipulationMessages.getString("TextRange.length")); //$NON-NLS-1$
		buffer.append(fLength);
		return buffer.toString();
	}

	public boolean equals(Object obj) {
		if (! (obj instanceof TextRange))
			return false;
		TextRange other= (TextRange)obj;	
		return fOffset == other.getOffset() && fLength == other.getLength();
	}

	public int hashCode() {
		return fOffset ^ fLength;
	}

}

