/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor.asm;

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
	/*
	 * @see org.eclipse.jface.text.rules.IWordDetector#isWordStart(char)
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
	/*
	 * @see org.eclipse.jface.text.rules.IWordDetector#isWordPart(char)
	 */
	public boolean isWordPart(char c) {
		return Character.isJavaIdentifierPart(c);
	}
}
