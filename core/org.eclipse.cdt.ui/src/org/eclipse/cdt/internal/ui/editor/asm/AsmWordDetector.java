package org.eclipse.cdt.internal.ui.editor.asm;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.text.rules.IWordDetector;


/**
 * A C aware word detector.
 */
public class AsmWordDetector implements IWordDetector {
	private char fPrefix = 0;
	private char fExtra = 0;
	private boolean fStrictStart = true;

	public AsmWordDetector() {
	}
	
	public AsmWordDetector(boolean strict) {
		fStrictStart = strict;
	}
	
	public AsmWordDetector(char extra) {
		fExtra = extra;
	}
	
	public AsmWordDetector(char prefix, char extra) {
		fPrefix = prefix;
		fExtra = extra;
	}
	/**
	 * @see IWordDetector#isWordIdentifierStart
	 */
	public boolean isWordStart(char c) {
		if(fPrefix != 0) {
			return (fPrefix == c);
		}
		if(fStrictStart) {
			return (Character.isJavaIdentifierStart(c) || (c == fExtra));
		}
		return (Character.isJavaIdentifierPart(c) || (c == fExtra));
	}
	
	/**
	 * @see IWordDetector#isWordIdentifierPart
	 */
	public boolean isWordPart(char c) {
		return Character.isJavaIdentifierPart(c);
	}
}
