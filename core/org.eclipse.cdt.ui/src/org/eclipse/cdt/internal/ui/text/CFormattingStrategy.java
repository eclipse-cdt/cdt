/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;


import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.formatter.IFormattingStrategy;
import org.eclipse.jface.text.source.ISourceViewer;


import org.eclipse.cdt.internal.formatter.CCodeFormatter;


public class CFormattingStrategy implements IFormattingStrategy {


	private String fInitialIndentation;
	private ISourceViewer fViewer;	


	public CFormattingStrategy(ISourceViewer viewer) {
		fViewer = viewer;
	}
	/**
	 * @see IFormattingStrategy#format(String, boolean, String, int[])
	 */
	public String format(String content, boolean isLineStart, String indentation, int[] positions) {
		//ConfigurableOption[] options= CUIPlugin.getDefault().getCodeFormatterOptions();
		CCodeFormatter formatter= new CCodeFormatter(/* null options */);
		
		//IDocument doc= fViewer.getDocument();
		//String lineDelimiter= getLineDelimiterFor(doc);
		//formatter.options.setLineSeparator(lineDelimiter);


		//formatter.setPositionsToMap(positions);
		return formatter.formatSourceString(content);
	}
	/**
	 * @see IFormattingStrategy#formatterStarts(String)
	 */
	public void formatterStarts(String initialIndentation) {
		fInitialIndentation= initialIndentation;
	}
	/**
	 * @see IFormattingStrategy#formatterStops()
	 */
	public void formatterStops() {
	}
	
	/**
	 * Embodies the policy which line delimiter to use when inserting into
	 * a document
	 */	
	private static String getLineDelimiterFor(IDocument doc) {
		String lineDelim= null;
		try {
			lineDelim= doc.getLineDelimiter(0);
		} catch (BadLocationException e) {
		}
		if (lineDelim == null) {
			String systemDelimiter= System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			String[] lineDelims= doc.getLegalLineDelimiters();
			for (int i= 0; i < lineDelims.length; i++) {
				if (lineDelims[i].equals(systemDelimiter)) {
					lineDelim= systemDelimiter;
					break;
				}
			}
			if (lineDelim == null) {
				lineDelim= lineDelims.length > 0 ? lineDelims[0] : systemDelimiter;
			}
		}
		return lineDelim;
	}


}


