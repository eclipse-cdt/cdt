/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/

package org.eclipse.cdt.internal.core.parser.scanner;

import org.eclipse.cdt.core.parser.CodeReader;

/**
 * @author jcamelon
 */
public class LimitedScannerContext
	extends ScannerContextInclusion
	implements IScannerContext {

	private final int limit;
	private final Scanner scanner;

	/**
	 * @param reader
	 * @param string
	 * @param i
	 * @param object
	 * @param offsetLimit
	 */
	public LimitedScannerContext(Scanner scanner, CodeReader code, int offsetLimit, int index ) {
		super( code, null, index );
		this.scanner = scanner;
		limit = offsetLimit;
	}

	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IScannerContext#read()
	 */
	public int getChar()  {
		if( getOffset() == limit )
		{
			scanner.setOffsetLimitReached(true);
			return -1;
		}
		return super.getChar();
	}

	public int getKind() {
		return ScannerContextInclusion.ContextKind.TOP;
	}
}
