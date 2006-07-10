/*******************************************************************************
 * Copyright (c) 2000 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;


import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;


/**
 * Reads the text contents from a reader of HTML contents and translates 
 * the tags or cut them out.
 */
public class HTML2TextReader extends SubstitutionTextReader {
	private static final Map fgEntityLookup;
	private static final Set fgTags;
	
	static {
		
		fgTags= new HashSet();
		fgTags.add("b"); //$NON-NLS-1$
		fgTags.add("br"); //$NON-NLS-1$
		fgTags.add("h5"); //$NON-NLS-1$
		fgTags.add("p"); //$NON-NLS-1$
		fgTags.add("dl"); //$NON-NLS-1$
		fgTags.add("dt"); //$NON-NLS-1$
		fgTags.add("dd"); //$NON-NLS-1$
		fgTags.add("li"); //$NON-NLS-1$
		fgTags.add("ul"); //$NON-NLS-1$
		
		fgEntityLookup= new HashMap(7);
		fgEntityLookup.put("lt", "<"); //$NON-NLS-1$ //$NON-NLS-2$
		fgEntityLookup.put("gt", ">"); //$NON-NLS-1$ //$NON-NLS-2$
		fgEntityLookup.put("nbsp", " "); //$NON-NLS-1$ //$NON-NLS-2$
		fgEntityLookup.put("amp", "&"); //$NON-NLS-1$ //$NON-NLS-2$
		fgEntityLookup.put("circ", "^"); //$NON-NLS-1$ //$NON-NLS-2$
		fgEntityLookup.put("tilde", "~"); //$NON-NLS-2$ //$NON-NLS-1$
		fgEntityLookup.put("quot", "\"");		 //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	private int fCounter= 0;
	private TextPresentation fTextPresentation;
	private int fBold= 0;
	private int fStartOffset= -1;
	private boolean fInParagraph= false;

	/**
	 * Transforms the html text from the reader to formatted text.
	 * @param presentation If not <code>null</code>, formattings will be applied to 
	 * the presentation.
	*/
	public HTML2TextReader(Reader reader, TextPresentation presentation) {
		super(new PushbackReader(reader));
		fTextPresentation= presentation;
	}
	
	public int read() throws IOException {
		int c= super.read();
		if (c != -1)
			++ fCounter;
		return c;
	}
	
	protected void startBold() {
		if (fBold == 0)
			fStartOffset= fCounter;
		++ fBold;
	}
	
	protected void stopBold() {
		-- fBold;
		if (fBold == 0) {
			if (fTextPresentation != null) {
				fTextPresentation.addStyleRange(new StyleRange(fStartOffset, fCounter - fStartOffset, null, null, SWT.BOLD));
			}
			fStartOffset= -1;
		}
	}
	
	/**
	 * @see SubstitutionTextReader#computeSubstitution(char)
	 */
	protected String computeSubstitution(int c) throws IOException {
		if (c == '<')
			return  processHTMLTag();
		else if (c == '&')
			return processEntity();
		
		return null;
	}

	private String html2Text(String html) {
		
		String tag= html;
		if ('/' == tag.charAt(0))
			tag= tag.substring(1);
			
		if (!fgTags.contains(tag))
			return ""; //$NON-NLS-1$

		if ("b".equals(html)) { //$NON-NLS-1$
			startBold();
			return ""; //$NON-NLS-1$
		}
				
		if ("h5".equals(html) || "dt".equals(html)) { //$NON-NLS-1$ //$NON-NLS-2$
			startBold();
			return ""; //$NON-NLS-1$
		}
		
		if ("dl".equals(html)) //$NON-NLS-1$
			return LINE_DELIM;
		
		if ("dd".equals(html)) //$NON-NLS-1$
			return "\t"; //$NON-NLS-1$
		
		if ("li".equals(html)) //$NON-NLS-1$
			return LINE_DELIM + "\t" + "-"; //$NON-NLS-1$ //$NON-NLS-2$
					
		if ("/b".equals(html)) { //$NON-NLS-1$
			stopBold();
			return ""; //$NON-NLS-1$
		}

		if ("p".equals(html))  { //$NON-NLS-1$
			fInParagraph= true;
			return LINE_DELIM;
		}

		if ("br".equals(html)) //$NON-NLS-1$
			return LINE_DELIM;
		
		if ("/p".equals(html))  { //$NON-NLS-1$
			boolean inParagraph= fInParagraph;
			fInParagraph= false;
			return inParagraph ? "" : LINE_DELIM; //$NON-NLS-1$
		}
			
		if ("/h5".equals(html) || "/dt".equals(html)) { //$NON-NLS-1$ //$NON-NLS-2$
			stopBold();
			return LINE_DELIM;
		}
		
		if ("/dd".equals(html)) //$NON-NLS-1$
			return LINE_DELIM;
				
		return ""; //$NON-NLS-1$
	}
	
	/*
	 * A '<' has been read. Process a html tag
	 */ 
	private String processHTMLTag() throws IOException {
		
		StringBuffer buf= new StringBuffer();
		int ch;
		do {		
			
			ch= nextChar();
			
			while (ch != -1 && ch != '>') {
				buf.append(Character.toLowerCase((char) ch));
				ch= nextChar();
				if (ch == '"'){
					buf.append(Character.toLowerCase((char) ch));
					ch= nextChar();
					while (ch != -1 && ch != '"'){
						buf.append(Character.toLowerCase((char) ch));
						ch= nextChar();
					}	
				}
				if (ch == '<'){
					unread(ch);
					return '<' + buf.toString();
				}	
			}
			
			if (ch == -1)
				return null;
			
			int tagLen= buf.length();
			// needs special treatment for comments 
			if ((tagLen >= 3 && "!--".equals(buf.substring(0, 3))) //$NON-NLS-1$
				&& !(tagLen >= 5 && "--!".equals(buf.substring(tagLen - 3)))) { //$NON-NLS-1$
				// unfinished comment
				buf.append(ch);
			} else {
				break;
			}
		} while (true);
		 
		return html2Text(buf.toString());
	}
	
	private void unread(int ch) throws IOException {
		((PushbackReader) getReader()).unread(ch);
	}
	
	protected String entity2Text(String symbol) {
		if (symbol.length() > 1 && symbol.charAt(0) == '#') {
			int ch;
			try {
				if (symbol.charAt(1) == 'x') {
					ch= Integer.parseInt(symbol.substring(2), 16);
				} else {
					ch= Integer.parseInt(symbol.substring(1), 10);
				}
				return "" + (char)ch; //$NON-NLS-1$
			} catch (NumberFormatException e) {
			}
		} else {
			String str= (String) fgEntityLookup.get(symbol);
			if (str != null) {
				return str;
			}
		}
		return "&" + symbol; // not found //$NON-NLS-1$
	}
	
	/*
	 * A '&' has been read. Process a entity
	 */ 	
	private String processEntity() throws IOException {
		StringBuffer buf= new StringBuffer();
		int ch= nextChar();
		while (Character.isLetterOrDigit((char)ch) || ch == '#') {
			buf.append((char) ch);
			ch= nextChar();
		}
		
		if (ch == ';') 
			return entity2Text(buf.toString());
		
		buf.insert(0, '&');
		if (ch != -1)
			buf.append((char) ch);
		return buf.toString();
	}
}