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
public class IntegerExpansionToken extends SimpleExpansionToken	implements INumericToken
{
	private final long value;

	/**
	 * @param tokenType
	 * @param value
	 * @param stack
	 */
	public IntegerExpansionToken(int tokenType, long l, ContextStack stack) {
		super( tokenType, stack );
		this.value = l;
	}
	
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IToken#getImage()
	 */
	public String getImage() {
		return Long.toString( value );
	}



	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.INumericToken#getIntegerValue()
	 */
	public long getIntegerValue() {
		return value;
	}
}
