/*******************************************************************************
 * Copyright (c) 2001, 2004 IBM - Rational Software and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM - Rational Software - initial implementation
 ******************************************************************************/

package org.eclipse.cdt.internal.core.parser.scanner;

import java.io.IOException;
import java.io.Reader;

import org.eclipse.cdt.core.parser.ast.IASTInclusion;

public class ScannerContextInclusion implements IScannerContext
{
	protected Reader reader;
	private String filename;
	private IASTInclusion inc;
	private final int index;
	private int line;
	protected int offset = 0;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.IScannerContext#initialize(Reader, String, int, IASTInclusion)
     */
    public ScannerContextInclusion(Reader r, String f, IASTInclusion i, int index) {
    	reader = r;
		filename = f;
		line = 1;
		inc = i;
        this.index = index;
    }
		
	public final String getContextName()
	{
		return filename;
	}
	public int getOffset()
	{
		return offset - pos;
	}
	public void close() {
		try {
			reader.close();
		}
		catch (IOException ie) {
		}
	}
	protected int pos = 0;
	protected int undo[] = new int[2];  
	public final void ungetChar(int c) {
		undo[pos++] = c; 
	}
	
	public int getChar() {
		if (pos > 0)
			return undo[--pos];
		try {
			++offset;
			int c = reader.read();
			if ((char)c == '\n') line++;
			return c;
		}
	   	catch (IOException e) {
    		return -1;
    	}
	}
	
	/**
	 * Returns the kind.
	 * @return int
	 */
	public int getKind() {
		return ScannerContextInclusion.ContextKind.INCLUSION;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IScannerContext#getExtension()
	 */
	public IASTInclusion getExtension() {
		return inc;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerContext#getFilenameIndex()
	 */
	public int getFilenameIndex() {
		return index;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IScannerContext#getLine()
	 */
	public final int getLine()
	{
		return line;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append( "file "); //$NON-NLS-1$
		buffer.append( getContextName() );
		buffer.append( ":"); //$NON-NLS-1$
		buffer.append( getLine() );
		return buffer.toString();
	}
}
