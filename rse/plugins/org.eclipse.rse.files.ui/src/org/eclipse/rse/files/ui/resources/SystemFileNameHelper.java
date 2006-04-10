/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.files.ui.resources;

import java.io.File;

import org.eclipse.rse.model.SystemEscapeCharHelper;


/**
 * This class converts a remote file name (including path) to a valid local one by 
 * escaping invalid characters in local file names with an _xx escape sequence.
 */
public class SystemFileNameHelper {
	
	

	protected static final char[] array = {' ', ':', ';', '*', '?', '\'', '"', '<', '>', '|'};

	

	
	/**
	 * Get the escaped path name.  Changes unsupported characters to _xyz.
	 */
	public static String getEscapedPath(String path) 
	{	
		char c = File.separatorChar;
		
		// NOTE: if it's Linux, Unix or iSeries, we don't like '\\' character, i.e. escape it
		if (c == '/') {
			c = '\\';
		}
		// or if Windows, we don't like '/' character, i.e. escape it
		else if (c == '\\') 
		{
			c = '/';
		}
		
		// first escape char
		array[0] = c;
		
		
		SystemEscapeCharHelper helper = new SystemEscapeCharHelper(array);
		
		int index = path.indexOf(':');
		
		// DKM - 56907
		if (index == -1 || path.charAt(0)==File.separatorChar)
		{
			return helper.getStringForFileName(path);
		}
		else 
		{
			return (path.substring(0, index + 1) + helper.getStringForFileName(path.substring(index + 1)));
		}	
	}
	
	/**
	 * Gets the unescaped path name.  Changes all _xyz to the original unsupported character.
	 */
	public static String getUnescapedPath( String path ){
		SystemEscapeCharHelper helper = new SystemEscapeCharHelper(array);
		return helper.getStringFromFileName( path );
	}
}