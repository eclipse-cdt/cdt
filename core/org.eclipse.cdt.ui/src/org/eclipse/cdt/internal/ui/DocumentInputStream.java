package org.eclipse.cdt.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
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
	 * @see InputStream#read
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