package org.eclipse.cdt.internal.corext.textmanipulation;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * A <code>SwapTextEdit</code> exchanges to text ranges.
 */
public class SwapTextEdit extends MultiTextEdit {
	/**
	 * Create a new <code>SwapTextEdit</code>
	 * 
	 * @param offset1 the offset of the first text range
	 * @param length1 the length of the first text range
	 * @param offset2 the offset of the second text range
	 * @param length2 the length of the second text range
	 */
	public SwapTextEdit(int offset1, int length1, int offset2, int length2) {
		add(new MoveTextEdit(offset1, length1, offset2));
		add(new MoveTextEdit(offset2, length2, offset1));
	}
}

