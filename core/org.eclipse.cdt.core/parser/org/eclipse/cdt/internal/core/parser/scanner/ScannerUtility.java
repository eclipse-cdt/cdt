/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/package org.eclipse.cdt.internal.core.parser.scanner;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.NullSourceElementRequestor;
import org.eclipse.cdt.core.parser.ScannerException;
import org.eclipse.cdt.core.parser.extension.IScannerExtension;

/**
 * @author jcamelon
 */
public class ScannerUtility {

	/**
	 * @param string
	 * @return
	 */
	static String reconcilePath(String originalPath ) {
		if( originalPath == null ) return null;
		String [] segments = originalPath.split( "[/\\\\]" ); //$NON-NLS-1$
		if( segments.length == 1 ) return originalPath;
		Vector results = new Vector(); 
		for( int i = 0; i < segments.length; ++i )
		{
			String segment = segments[i];
			if( segment.equals( ".") ) continue; //$NON-NLS-1$
			if( segment.equals("..") ) //$NON-NLS-1$
			{
				if( results.size() > 0 ) 
					results.removeElementAt( results.size() - 1 );
			}
			else
				results.add( segment );
		}
		StringBuffer buffer = new StringBuffer(); 
		Iterator i = results.iterator();
		while( i.hasNext() )
		{
			buffer.append( (String)i.next() );
			if( i.hasNext() )
				buffer.append( File.separatorChar );
		}
		return buffer.toString();
	}

	
	static CodeReader createReaderDuple( String path, String fileName, ISourceElementRequestor requestor, Iterator workingCopies )
	{
		String finalPath = createReconciledPath(path, fileName);
		Reader r = requestor.createReader( finalPath, workingCopies	);
		if( r != null )
			return new CodeReader( finalPath, r );
		return null;		
	}
	
	/**
	 * @param path
	 * @param fileName
	 * @return
	 */
	static String createReconciledPath(String path, String fileName) {
		//TODO assert pathFile.isDirectory();	
		StringBuffer newPathBuffer = new StringBuffer( new File(path).getPath() );
		newPathBuffer.append( File.separatorChar );
		newPathBuffer.append( fileName );
		//remove ".." and "." segments
		return reconcilePath( newPathBuffer.toString() );
	}

	static class InclusionDirective
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
		
		String getFilename()
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
	
	static class InclusionParseException extends Exception
	{
	}
	
	private static final ISourceElementRequestor NULL_REQUESTOR = new NullSourceElementRequestor();
	private static final IParserLogService NULL_LOG_SERVICE = new NullLogService();
	private static final InclusionParseException INCLUSION_PARSE_EXCEPTION  = new InclusionParseException(); 
	
	static InclusionDirective parseInclusionDirective( IScannerData scannerData, IScannerExtension scannerExtension, String includeLine, int baseOffset ) throws InclusionParseException 
	{
		try
		{
			boolean useIncludePath = true;
			StringBuffer fileNameBuffer = new StringBuffer();
			int startOffset = baseOffset, endOffset = baseOffset;
			
			if (! includeLine.equals("")) { //$NON-NLS-1$
				Scanner helperScanner = new Scanner(
											new StringReader(includeLine), 
											null, 
											scannerData.getDefinitions(), scannerData.getIncludePathNames(),
											NULL_REQUESTOR,
											scannerData.getParserMode(),
											scannerData.getLanguage(), NULL_LOG_SERVICE, (IScannerExtension)(scannerExtension.clone()) );
				helperScanner.setForInclusion( true );
				IToken t = null;
				
				try {
					t = helperScanner.nextToken(false);
				} catch (EndOfFileException eof) {
					throw INCLUSION_PARSE_EXCEPTION ;
				} 
	
				try {
					if (t.getType() == IToken.tSTRING) {
						fileNameBuffer.append(t.getImage());
						startOffset = baseOffset + t.getOffset();
						endOffset = baseOffset + t.getEndOffset();
						useIncludePath = false;
						
						// This should throw EOF
						t = helperScanner.nextToken(false);
						throw INCLUSION_PARSE_EXCEPTION ;
					} else if (t.getType() == IToken.tLT) {
						
						try {
												
							t = helperScanner.nextToken(false);
							startOffset = baseOffset + t.getOffset();
							
							while (t.getType() != IToken.tGT) {
								fileNameBuffer.append(t.getImage());
								helperScanner.skipOverWhitespace();
								int c = helperScanner.getChar();
								if (c == '\\') fileNameBuffer.append('\\'); else helperScanner.ungetChar(c);
								t = helperScanner.nextToken(false);
							}
							
							endOffset = baseOffset + t.getEndOffset();
							
						} catch (EndOfFileException eof) {
							throw INCLUSION_PARSE_EXCEPTION ;
						}
						
						// This should throw EOF
						t = helperScanner.nextToken(false);
						throw INCLUSION_PARSE_EXCEPTION ;
						
					} else 
						throw INCLUSION_PARSE_EXCEPTION ;
				}
				catch( EndOfFileException eof )
				{
					// good
				} 
				
			} else
				throw INCLUSION_PARSE_EXCEPTION ;
	
			return new InclusionDirective( fileNameBuffer.toString(), useIncludePath, startOffset, endOffset );
		}
		catch( ScannerException se )
		{
			throw INCLUSION_PARSE_EXCEPTION ;
		}

	}
}
