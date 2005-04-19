/**********************************************************************
 * Copyright (c) 2002-2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner2;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;

/**
 * @author jcamelon
 *
 */
public class ScannerUtility {

	public static String reconcilePath(String originalPath ) {
		if( originalPath == null ) return null;
		originalPath = removeQuotes( originalPath );
		
		String [] segments = originalPath.split( "[/\\\\]" ); //$NON-NLS-1$
		if( segments.length == 1 ) return originalPath;
		Vector results = new Vector(segments.length); 
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
		StringBuffer strbuff = new StringBuffer(128); 
		for( int i = 0; i < results.size(); ++i )
		{
			strbuff.append( (String)results.elementAt(i) );
			if( i != results.size() - 1  )
				strbuff.append( File.separatorChar );
		}
		return strbuff.toString();
	}

	
	/**
	 * @param originalPath
	 * @return
	 */
	private static String removeQuotes(String originalPath) {
		String [] segments = originalPath.split( "\""); //$NON-NLS-1$
		if( segments.length == 1 ) return originalPath;
		StringBuffer strbuff = new StringBuffer();
		for( int i = 0; i < segments.length; ++ i )
			if( segments[i] != null )
				strbuff.append( segments[i]);
		return strbuff.toString();
	}


	public static CodeReader createReaderDuple( String path, ISourceElementRequestor requestor, Iterator workingCopies )
	{
		return requestor.createReader( path, workingCopies );
	}
	
	/**
	 * @param path
	 * @param fileName
	 * @return
	 */
	public static String createReconciledPath(String path, String fileName) {
		//TODO assert pathFile.isDirectory();	
		StringBuffer newPathBuffer = new StringBuffer();
        if( ! path.equals( "" )) //$NON-NLS-1$
        {
            newPathBuffer.append( new File(path).getPath() );
            newPathBuffer.append( File.separatorChar );
        }
		newPathBuffer.append( fileName );
		//remove ".." and "." segments
		return reconcilePath( newPathBuffer.toString() );
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
