package org.eclipse.cdt.internal.ui.text.util;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.text.rules.IWordDetector;


/**
 * A C aware word detector.
 */
public class CWordDetector implements IWordDetector {


	/**
	 * @see IWordDetector#isWordIdentifierStart
	 */
	public boolean isWordStart(char c) {
		return Character.isJavaIdentifierStart(c);
	}
	
	/**
	 * @see IWordDetector#isWordIdentifierPart
	 */
	public boolean isWordPart(char c) {
		return Character.isJavaIdentifierPart(c);
	}
}
