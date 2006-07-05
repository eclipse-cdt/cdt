/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.ui;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
/**
 * Input stream which reads from a document
 */
public class DocumentInputStream extends InputStream {
	
	private IDocument fDocument;
	private int fCurrPos;
	
	public DocumentInputStream(IDocument document) {
		fDocument= document;
		fCurrPos= 0;
	}
	
	public IDocument getDocument() {
		return fDocument;
	}
		
	/**
	 * {@inheritDoc}
	 */
	 public int read() throws IOException {
	 	try {
		 	if (fCurrPos < fDocument.getLength()) {
		 	 	return fDocument.getChar(fCurrPos++);
		 	}
	 	} catch (BadLocationException e) {
	 	}
	 	return -1;
	}
	
}