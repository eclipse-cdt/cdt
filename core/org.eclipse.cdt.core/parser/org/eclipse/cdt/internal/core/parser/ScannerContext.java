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

public class ScannerContext implements IScannerContext
{
	private Reader reader;
	private String filename;
	private int offset;
	private int undo; 
		
	public ScannerContext(){}
	public IScannerContext initialize(Reader r, String f, int u )
	{
		reader = r;
		filename = f;
		offset = 0;
		undo = u;
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

	/**
	 * Returns the undo.
	 * @return int
	 */
	public final int getUndo()
	{
		return undo;
	}

	/**
	 * Sets the undo.
	 * @param undo The undo to set
	 */
	public void setUndo(int undo)
	{
		this.undo= undo;
	}

}
