package org.eclipse.cdt.internal.ui.text;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.internal.formatter.CCodeFormatter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.formatter.IFormattingStrategy;
import org.eclipse.jface.text.source.ISourceViewer;


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


