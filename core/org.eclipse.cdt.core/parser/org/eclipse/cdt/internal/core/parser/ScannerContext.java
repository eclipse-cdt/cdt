/*******************************************************************************
 * Copyright (c) 2001 Rational Software Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     Rational Software - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.core.parser;

import java.io.IOException;
import java.io.Reader;
import java.util.Stack;

import org.eclipse.cdt.core.parser.ast.IASTInclusion;

public class ScannerContext implements IScannerContext
{
	private Reader reader;
	private String filename;
	private int offset;
	private Stack undo = new Stack(); 
	private int kind; 
				
	public ScannerContext(){}
	public IScannerContext initialize(Reader r, String f, int k, IASTInclusion i)
	{
		reader = r;
		filename = f;
		offset = 0;
		kind = k; 
		inc = i;		
		return this;
	}
		
	public int read() throws IOException {
		++offset;
		return reader.read();
	}
	
	/**
	 * Returns the filename.
	 * @return String
	 */
	public final String getFilename()
	{
		return filename;
	}

	/**
	 * Returns the offset.
	 * @return int
	 */
	public final int getOffset()
	{
		return offset;
	}

	/**
	 * Returns the reader.
	 * @return Reader
	 */
	public final Reader getReader()
	{
		return reader;
	}

	public final int undoStackSize()
	{
		return undo.size();
	}

	/**
	 * Returns the undo.
	 * @return int
	 */
	public final int popUndo()
	{
		return ((Integer)undo.pop()).intValue();
	}

	/**
	 * Sets the undo.
	 * @param undo The undo to set
	 */
	public void pushUndo(int undo)
	{
		this.undo.push( new Integer( undo )); 
	}


	/**
	 * Returns the kind.
	 * @return int
	 */
	public int getKind() {
		return kind;
	}

	/**
	 * Sets the kind.
	 * @param kind The kind to set
	 */
	public void setKind(int kind) {
		this.kind = kind;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IScannerContext#getExtension()
	 */
	public IASTInclusion getExtension() {
		return inc;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IScannerContext#setExtension(org.eclipse.cdt.core.parser.ast.IASTInclusion)
	 */
	public void setExtension(IASTInclusion ext) {
		inc = ext;
	}
	
	private IASTInclusion inc = null;


}
