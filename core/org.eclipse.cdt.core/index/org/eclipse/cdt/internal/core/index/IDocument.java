/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 ******************************************************************************/
/*
 * Created on May 30, 2003
 */
package org.eclipse.cdt.internal.core.index;

import java.io.IOException;

/**
 * An <code>IDocument</code> represent a data source, e.g.&nbsp;a <code>File</code> (<code>FileDocument</code>), 
 * an <code>IFile</code> (<code>IFileDocument</code>), 
 * or other kinds of data sources (URL, ...). An <code>IIndexer</code> indexes an<code>IDocument</code>.
 */

public interface IDocument {
	/**
	 * Returns the content of the document, in a byte array.
	 */
	byte[] getByteContent() throws IOException;
	/**
	 * Returns the content of the document, in a char array.
	 */
	char[] getCharContent() throws IOException;
	/**
	 * Returns the encoding for this document
	 */
	String getEncoding();
	/**
	 * returns the name of the document (e.g. its path for a <code>File</code>, or its relative path
	 * in the workbench for an <code>IFile</code>).
	 */
	String getName();
	/**
	 * Returns the content of the document, as a String.
	 */
	public String getStringContent() throws IOException;
	/**
	 * Returns the type of the document.
	 */
	String getType();
}
