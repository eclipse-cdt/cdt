/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.token;

import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.internal.core.parser.scanner.ContextStack;

/**
 * @author johnc
 */
public class SimpleExpansionToken extends SimpleToken implements IToken {

	/**
	 * @param tokenType
	 * @param stack
	 */
	public SimpleExpansionToken(int tokenType, ContextStack stack, char [] f) {
		super( tokenType, stack, f );
	}
	
    public SimpleExpansionToken(int t, int macroOffset, int macroLength, char [] f, int l) {
        super(t, macroOffset + macroLength, f, l);
		setOffsetAndLength( macroOffset, macroLength );
	}
	protected int length;
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.token.SimpleToken#setOffsetAndLength(org.eclipse.cdt.internal.core.parser.scanner.IScannerContext)
	 */
	protected void setOffsetAndLength(int macroOffset, int macroLength ) {
		offset = macroOffset;
		length = macroLength;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.token.AbstractToken#getLength()
	 */
	public final int getLength() {
		return length;
	}
}
