/*
 * Created on Dec 4, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.cdt.internal.core.parser;

import java.io.IOException;
import java.io.Reader;

/**
 * @author jcamelon
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
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
