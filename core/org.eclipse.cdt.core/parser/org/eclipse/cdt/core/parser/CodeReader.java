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
package org.eclipse.cdt.core.parser;

import java.io.Reader;

/**
 * @author jcamelon
 */
public class CodeReader {
	
	public CodeReader( String filename, Reader reader )
	{
		this.reader = reader;
		this.filename = filename;
	}
	
	private final Reader reader;
	private final String filename;

	public String getFilename()
	{
		return filename;
	}
	
	public Reader getUnderlyingReader()
	{
		return reader;
	}
}
