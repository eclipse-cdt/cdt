/*******************************************************************************
 * Copyright (c) 2000 - 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.token;

import org.eclipse.cdt.core.parser.INumericToken;
import org.eclipse.cdt.internal.core.parser.scanner.ContextStack;

/**
 * @author jcamelon
 */
public class HexIntegerExpansionToken extends SimpleExpansionToken implements
		INumericToken {

	private final long intValue;

	/**
	 * @param i
	 * @param value
	 * @param stack
	 */
	public HexIntegerExpansionToken(int type, String value, ContextStack stack) {
		super( type, stack );
		int minIndex = findMinIndex(value);
		if( minIndex == -1 )
			intValue = Long.parseLong(value.substring(2), 16 );
		else
			intValue = Long.parseLong(value.substring(2, minIndex), 16 );
		setOffsetAndLength(stack.getCurrentContext());
	}
	
	/**
	 * @param value
	 * @return
	 */
	private int findMinIndex(String value) {
		int endIndex = value.indexOf( "U"); //$NON-NLS-1$
		int endIndex2 = value.indexOf( "L"); //$NON-NLS-1$
		int minIndex = endIndex < endIndex2 ? endIndex : endIndex2;
		return minIndex;
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IToken#getImage()
	 */
	public String getImage() {
		StringBuffer buffer = new StringBuffer( "0x" ); //$NON-NLS-1$
		buffer.append( Long.toHexString(intValue) );
		return buffer.toString();
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.INumericToken#getIntegerValue()
	 */
	public long getIntegerValue() {
		return intValue;
	}
}
