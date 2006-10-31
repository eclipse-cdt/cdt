/********************************************************************************
 * Copyright (c) 2006 IBM Corporation and Wind River Systems, Inc. All rights reserved.
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
 * Martin Oberhuber (Wind River) - Fix for bug 161844 - regex matching backslashes
 * 
 ********************************************************************************/

package org.eclipse.rse.services.clientserver;

import org.eclipse.rse.services.clientserver.archiveutils.AbsoluteVirtualPath;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;

public class PathUtility 
{

	public static String normalizeWindows(String path)
	{
		if (path == null || path.length() < 2) return path;
		boolean containsForwardSlash = false;
		boolean containsDoubleSlashes = false;
		boolean endsWithSlash = false;
		
		//TODO Improve performance by using a pre-compiled Regex Pattern
		if (path.indexOf('/') != -1) containsForwardSlash = true;
		if (path.indexOf("\\\\") != -1) containsDoubleSlashes = true; //$NON-NLS-1$
		if (path.endsWith("\\") || path.endsWith("/")) endsWithSlash = true; //$NON-NLS-1$ //$NON-NLS-2$
		
		boolean needsNormalizing = containsForwardSlash || containsDoubleSlashes || endsWithSlash;
		if (!needsNormalizing) return path;
		
		if (containsForwardSlash)
		{
			path = path.replace('/', '\\');
			containsDoubleSlashes = (path.indexOf("\\\\") != -1); //$NON-NLS-1$
		}
		
		while (containsDoubleSlashes)
		{
			//TODO Improve performance by manually iterating over char array
			//need to quote once for the string, then again for the regex
			//Replace "\\" by "\": Regex matcher needs quoting twice in search, once in replacement
			path = path.replaceAll("\\\\\\\\", "\\\\"); //$NON-NLS-1$ //$NON-NLS-2$
			containsDoubleSlashes = (path.indexOf("\\\\") != -1); //$NON-NLS-1$
		}
		if (endsWithSlash)
		{
			if (!(path.length() == 3)) path = path.substring(0, path.length() - 1);
		}
		return path;
	}
	
	public static String normalizeUnix(String path)
	{
		if (path == null || path.length() < 2) return path;
		boolean containsBackSlash = false;
		boolean containsDoubleSlashes = false;
		boolean endsWithSlash = false;
		
		//TODO Improve performance by using a pre-compiled Regex Pattern
		if (path.indexOf('\\') != -1) containsBackSlash = true;
		if (path.indexOf("//") != -1) containsDoubleSlashes = true; //$NON-NLS-1$
		if (path.endsWith("\\") || path.endsWith("/")) endsWithSlash = true; //$NON-NLS-1$ //$NON-NLS-2$

		boolean needsNormalizing = containsBackSlash || containsDoubleSlashes || endsWithSlash;
		if (!needsNormalizing) return path;
		
		if (containsBackSlash)
		{
			path = path.replace('\\', '/');
			containsDoubleSlashes = (path.indexOf("//") != -1); //$NON-NLS-1$
		}
		
		while (containsDoubleSlashes)
		{
			//TODO Improve performance by manually iterating over char array
			path = path.replaceAll("//", "/"); //$NON-NLS-1$ //$NON-NLS-2$
			containsDoubleSlashes = (path.indexOf("//") != -1); //$NON-NLS-1$
		}
		
		if (endsWithSlash)
		{
			if (!(path.length() == 1)) path = path.substring(0, path.length() - 1);
		}
		return path;

	}
	
	public static String normalizeVirtualWindows(String path)
	{
		if (path == null || path.length() < 2) return path;
		AbsoluteVirtualPath avp = new AbsoluteVirtualPath(path);
		String realPart = avp.getContainingArchiveString();
		if (ArchiveHandlerManager.isVirtual(realPart))
			realPart = normalizeVirtualWindows(realPart);
		else realPart = normalizeWindows(realPart);
		return realPart + ArchiveHandlerManager.VIRTUAL_SEPARATOR + avp.getVirtualPart();
	}
	
	public static String normalizeVirtualUnix(String path)
	{
		if (path == null || path.length() < 2) return path;
		AbsoluteVirtualPath avp = new AbsoluteVirtualPath(path);
		String realPart = avp.getContainingArchiveString();
		if (ArchiveHandlerManager.isVirtual(realPart))
			realPart = normalizeVirtualUnix(realPart);
		else realPart = normalizeUnix(realPart);
		return realPart + ArchiveHandlerManager.VIRTUAL_SEPARATOR + avp.getVirtualPart();		
	}
	
	public static String normalizeUnknown(String path)
	{
		if (path == null || path.length() < 2) return path;
		if (path.charAt(1) == ':')
			if (path.indexOf(ArchiveHandlerManager.VIRTUAL_CANONICAL_SEPARATOR) == -1)
				return normalizeWindows(path);
			else return normalizeVirtualWindows(path);
		else if (path.charAt(0) == '/')
			if (path.indexOf(ArchiveHandlerManager.VIRTUAL_CANONICAL_SEPARATOR) == -1)
				return normalizeUnix(path);
			else return normalizeVirtualUnix(path);
		else return path;
	}
	
	public static String getSeparator(String path)
	{
		if (path.length() > 1 && path.charAt(1) == ':')
		{
			return "\\"; //$NON-NLS-1$
		}
		else
		{
			return "/"; //$NON-NLS-1$
		}
	}
}