/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser;

import java.io.IOException;
import java.io.Reader;

/**
 * @author jcamelon
 */
public class LimitedScannerContext
	extends ScannerContext
	implements IScannerContext {

	private final int limit;

	/**
	 * @param reader
	 * @param string
	 * @param i
	 * @param object
	 * @param offsetLimit
	 */
	public LimitedScannerContext(Reader reader, String string, ContextKind kind, int offsetLimit) {
		super( reader, string, kind, null );
		limit = offsetLimit;
	}

	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IScannerContext#read()
	 */
	public int read() throws IOException {
		if( getOffset() == limit ) throw new IOException();
		return super.read();
	}

}
