/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.internal.scannerconfig;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;

/**
 * Implementation class for gathering the built-in compiler settings for 
 * MinGw-based targets. The paths are collected only in Win32 format. 
 * 
 * @since 2.0
 */
public class DefaultMinGWScannerInfoCollector extends DefaultGCCScannerInfoCollector {
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector#contributeToScannerConfig(org.eclipse.core.resources.IResource, java.util.List, java.util.List, java.util.List)
	 */
	public void contributeToScannerConfig(IResource resource, List includes, List symbols, Map extraInfo) {
		// This method will be called by the parser each time there is a new value
		Iterator pathIter = includes.listIterator();
		while (pathIter.hasNext()) {
			String path = (String) pathIter.next();
			if (!filterPath(path)) {
				getIncludePaths().add(path);
			}
		}
		
		// Now add the macros
		Iterator symbolIter = symbols.listIterator();
		while (symbolIter.hasNext()) {
			// See if it has an equals
			String[] macroTokens = ((String)symbolIter.next()).split(EQUALS);
			String macro = macroTokens[0].trim();
			String value = (macroTokens.length > 1) ? macroTokens[1].trim() : new String();
			getDefinedSymbols().put(macro, value);
		}	
	}

	/* (non-Javadoc)
	 * 
	 * @param path
	 * @return
	 */
	private boolean filterPath(String includePath) {
		// MinGW's compiler will return paths in both POSIX and Win32 format
		// We only want to store the Win32 variant like:
		// <drive_letter>:<path>
		// \\<unc_path>
		int firstColon = includePath.indexOf(':');	//$NON-NLS-1$
		if (firstColon == 1 && Character.isLetter(includePath.charAt(0))) {
			// <drive>:<path> is OK
			return false;
		}

		return true;
	}


}
