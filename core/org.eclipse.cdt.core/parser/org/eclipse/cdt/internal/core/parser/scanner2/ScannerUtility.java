/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner2;

import java.io.File;
import java.util.Iterator;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;

/**
 * @author jcamelon
 *
 */
public class ScannerUtility {

	static final char DOT    = '.';  //$NON-NLS-1$
	static final char SLASH  = '/';  //$NON-NLS-1$
	static final char BSLASH = '\\'; //$NON-NLS-1$
	static final char QUOTE  = '\"'; //$NON-NLS-1$

	/**
	 * This method is quick 1-pass path reconciler.
	 * Functions:
	 *   - replace "/" or "\" by system's separator
	 *   - replace multiple separators by single one
	 *   - skip "/./" 
	 *   - skip quotes
	 *   - process "/../" (skip previous directory level)    
	 * 
	 * @param originalPath - path to process
	 * @return             - reconciled path   
	 */
	public static String reconcilePath(String originalPath ) {
		int len = originalPath.length();
		int len1 = len - 1;         // to avoid multiple calculations
		int j = 0;                  // index for output array
		boolean noSepBefore = true; // to avoid duplicate separators
		
		char[] ein = new char[len];
		char[] aus = new char[len + 1];
		
		originalPath.getChars(0, len, ein, 0);
		for (int i=0; i<len; i++) {
			char c = ein[i]; 
			switch (c) {
			case QUOTE:	  // quotes are removed
				noSepBefore = true;
				break;
			case SLASH:     // both separators are processed  
			case BSLASH:    // in the same way
				if (noSepBefore) {
					noSepBefore = false;
					aus[j++] = File.separatorChar;
				}
				break;
			case DOT:
				// no separator before, not a 1st string symbol. 
				if (noSepBefore && j>0) 
					aus[j++] = c;
				else { // separator before "." ! 
					if (i < len1) {
						c = ein[i+1]; // check for next symbol
						// check for "/./" case
						if (c == SLASH || c == BSLASH) {
							// write nothing to output
							// skip the next symbol
							i++;
							noSepBefore = false;
						} 
						// symbol other than "." - write it also 
						else if (c != DOT) {
							i++;
							noSepBefore = true;
							aus[j++] = DOT;
							aus[j++] = c;
						}
						// we found "/.." sequence. Look ahead.
						else {
							// we found "/../" (or "/.." is at the end of string)
							// we should delete previous segment of output path 
							if (i == len1 || ein[i+2] == SLASH || ein[i+2] == BSLASH) {
								i+=2;
								noSepBefore = false;
								if (j > 1) { // there is at least 1 segment before
									int k = j - 2;
									while ( k >= 0 ) {
										if (aus[k] == File.separatorChar) break;
										k--;
									}
									j = k + 1; // set index to previous segment or to 0
								}
							}
							// Case "/..blabla" processed as usual
							else {
								i++;
								noSepBefore = true;
								aus[j++] = DOT;
								aus[j++] = DOT;
							}
						}
					} else 
					{} // do nothing when "." is last symbol
				}
				break;
			default: 
				noSepBefore = true;
				aus[j++] = c;
			}
		}
		return new String(aus, 0, j);
	}

	/**
	 * @param path     - include path
	 * @param fileName - include file name
	 * @return         - reconsiled path
	 */
	public static String createReconciledPath(String path, String fileName) {
		boolean pathEmpty = (path == null || path.length() == 0);
		return (pathEmpty ? fileName : reconcilePath(path + File.separatorChar + fileName));
	}

	public static CodeReader createReaderDuple( String path, ISourceElementRequestor requestor, Iterator workingCopies )
	{
		return requestor.createReader( path, workingCopies );
	}
	
	public static class InclusionDirective
	{
		public InclusionDirective( String fileName, boolean useIncludePaths, int startOffset, int endOffset )
		{
			this.fileName = fileName;
			this.useIncludePaths = useIncludePaths;
			this.startOffset = startOffset;
			this.endOffset = endOffset;
		}
		
		private final boolean useIncludePaths;
		private final String fileName;
		private final int startOffset;
		private final int endOffset; 

		boolean useIncludePaths()
		{
			return useIncludePaths;
		}
		
		public String getFilename()
		{
			return fileName;
		}
		
		int getStartOffset()
		{
			return startOffset;
		}
		
		int getEndOffset()
		{
			return endOffset;
		}
	}
	
	public static class InclusionParseException extends Exception
	{
	}

}
