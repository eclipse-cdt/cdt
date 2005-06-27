/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.token;

import org.eclipse.cdt.core.parser.IToken;

/**
 * @author johnc
 */
public class ImagedExpansionToken extends ImagedToken implements IToken {
	
	/**
	 * @param t
	 * @param contextStack
	 * @param i
	 * @param l
	 */
	public ImagedExpansionToken(int t, char[] i, int macroOffset, int macroLength, char [] f, int l) {
		super(t, i, macroOffset, f, l );
		setOffsetAndLength( macroOffset, macroLength );
	}

	protected void setOffsetAndLength(int macroOffset, int macroLength ) {
		offset = macroOffset;
		length = macroLength;
	}

	
	protected int length;
		
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.token.AbstractToken#getLength()
	 */
	public final int getLength() {
		return length;
	}

}
