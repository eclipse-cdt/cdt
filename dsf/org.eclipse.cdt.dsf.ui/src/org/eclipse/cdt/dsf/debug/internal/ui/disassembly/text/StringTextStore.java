/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.text;

import org.eclipse.jface.text.ITextStore;

/**
 * Readonly ITextStore implementation.
 */
public class StringTextStore implements ITextStore {

	private String fText = ""; //$NON-NLS-1$

	public StringTextStore() {
		super();
	}

	public StringTextStore(String text) {
		super();
		fText = text;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextStore#get(int)
	 */
	public char get(int offset) {
		return fText.charAt(offset);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextStore#get(int, int)
	 */
	public String get(int offset, int length) {
		if (length == fText.length()) {
			return fText;
		}
		return new String(fText.substring(offset, offset+length));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextStore#getLength()
	 */
	public int getLength() {
		return fText.length();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextStore#replace(int, int, java.lang.String)
	 */
	public void replace(int offset, int length, String text) {
		// unmodifiable
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextStore#set(java.lang.String)
	 */
	public void set(String text) {
		fText = text != null ? text : ""; //$NON-NLS-1$
	}

}
