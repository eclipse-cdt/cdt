/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems) - Fixed bug 48339
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

/**
 * Auto indent strategy for C/C++ multiline comments
 */
public class CCommentAutoIndentStrategy extends DefaultIndentLineAutoEditStrategy {

	public CCommentAutoIndentStrategy() {
	}

	/**
	 * Returns whether the text ends with one of the given search strings.
	 */
	private boolean endsWithDelimiter(IDocument d, String txt) {
		String[] delimiters= d.getLegalLineDelimiters();
		
		for (int i= 0; i < delimiters.length; i++) {
			if (txt.endsWith(delimiters[i]))
				return true;
		}
		
		return false;
	}

	/**
	 * Copies the indentation of the previous line and adds a star.
	 * If the comment just started on this line adds also a blank.
	 *
	 * @param d the document to work on
	 * @param c the command to deal with
	 */
	static void commentIndentAfterNewLine(IDocument d, DocumentCommand c) {
		
		if (c.offset == -1 || d.getLength() == 0)
			return;
			
		try {
			// find start of line
			IRegion info= d.getLineInformationOfOffset(c.offset);
			int start= info.getOffset();
				
			// find white spaces
			int end= findEndOfWhiteSpaceAt(d, start, c.offset);
							
			StringBuffer buf= new StringBuffer(c.text);
			if (end >= start) {	// 1GEYL1R: ITPJUI:ALL - java doc edit smartness not work for class comments
				// append to input
				buf.append(d.get(start, end - start));				
				if (end < c.offset) {
					if (d.getChar(end) == '/') {
						// comment started on this line
						buf.append(" * ");	 //$NON-NLS-1$
					} else if (d.getChar(end) == '*') {
						buf.append("* "); //$NON-NLS-1$
					}
				}			
			}

			
			c.text= buf.toString();
				
		} catch (BadLocationException excp) {
			// stop work
		}	
	}	
	
	static int findEndOfWhiteSpaceAt(IDocument document, int offset, int end) throws BadLocationException {
		while (offset < end) {
			char c= document.getChar(offset);
			if (c != ' ' && c != '\t') {
				return offset;
			}
			offset++;
		}
		return end;
	}

	static void commentIndentForCommentEnd(IDocument d, DocumentCommand c) {
		if (c.offset < 2 || d.getLength() == 0) {
			return;
		}
		try {
			if ("* ".equals(d.get(c.offset - 2, 2))) { //$NON-NLS-1$
				// modify document command
				c.length++;
				c.offset--;
			}					
		} catch (BadLocationException excp) {
			// stop work
		}
	}

	/*
	 * @see IAutoIndentStrategy#customizeDocumentCommand
	 */
	public void customizeDocumentCommand(IDocument d, DocumentCommand c) {
		if (c.length == 0 && c.text != null && endsWithDelimiter(d, c.text))
			commentIndentAfterNewLine(d, c);
		else if ("/".equals(c.text)) { //$NON-NLS-1$
			commentIndentForCommentEnd(d, c);
		}
	}
}

