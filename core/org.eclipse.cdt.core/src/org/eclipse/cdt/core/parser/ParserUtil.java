/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser;

import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.cdt.internal.core.model.IDebugLogConstants;
import org.eclipse.cdt.internal.core.parser.InternalParserUtil;
import org.eclipse.cdt.internal.core.parser.ParserLogService;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * @author jcamelon
 *
 */
public class ParserUtil
{
	
	public static IParserLogService getParserLogService()
	{
		return parserLogService;
	}
		
	private static IParserLogService parserLogService = new ParserLogService(IDebugLogConstants.PARSER );
	private static IParserLogService scannerLogService = new ParserLogService(IDebugLogConstants.SCANNER );

	/**
	 * @return
	 */
	public static IParserLogService getScannerLogService() {
		return scannerLogService;
	}
	
	public static Reader createReader( String finalPath )
	{
		// check to see if the file which this path points to points to an 
		// IResource in the workspace
		try
		{
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IPath path = new Path( finalPath );
			
			if( workspace.getRoot().getLocation().isPrefixOf( path ) )
				path = path.removeFirstSegments(workspace.getRoot().getLocation().segmentCount() );

			IResource result = workspace.getRoot().findMember(path);
			
			if( result != null && result.getType() == IResource.FILE )
			{
				BufferedInputStream bufferedStream = new BufferedInputStream( ((IFile) result).getContents() );
				InputStreamReader reader = new InputStreamReader( bufferedStream );
				return reader;
			}
		}
		catch( CoreException ce )
		{
		}
		return InternalParserUtil.createFileReader(finalPath);
	}
}
