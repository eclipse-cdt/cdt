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

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ast.IASTInclusion;

public class ScannerContextInclusion implements IScannerContext
{
	public static final int UNDO_BUFFER_SIZE = 4;
	public CodeReader code;
	private IASTInclusion inc;
	private final int index;
	private int line;
	protected int offset = 0;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.IScannerContext#initialize(Reader, String, int, IASTInclusion)
     */
    public ScannerContextInclusion(CodeReader code, IASTInclusion i, int index) {
    	this.code = code;
		line = 1;
		inc = i;
        this.index = index;
    }
		
    public boolean isFinal() { return false; }
    
	public final String getContextName()
	{
		return new String( code.filename );
	}
	
	public int getOffset()
	{
		return offset;
	}
	
	public void close() {
		//TODO remove close and replace by releasing from file cache
	}
	
	public int getChar() {
		if (offset == code.buffer.length)
			return -1;

		int c = code.buffer[offset++];
		if ((char)c == '\n') line++;
		return c;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerContext#ungetChar(int)
	 */
	public void ungetChar(int undo) {
		--offset;
		if (undo == '\n') line--;
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
