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

import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.CygpathTranslator;
import org.eclipse.core.resources.IResource;

/**
 * Implementation class for gathering the built-in compiler settings for 
 * Cygwin-based targets.
 * 
 * @since 2.0
 */
public class DefaultGnuWinScannerInfoCollector extends DefaultGCCScannerInfoCollector {
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector#contributeToScannerConfig(java.lang.Object, java.util.Map)
     */
    public void contributeToScannerConfig(Object resource, Map scannerInfo) {
        // check the resource
        if (resource != null && resource instanceof IResource &&
                ((IResource) resource).getProject() == project ) {
            List includes = (List) scannerInfo.get(ScannerInfoTypes.INCLUDE_PATHS);
            List symbols = (List) scannerInfo.get(ScannerInfoTypes.SYMBOL_DEFINITIONS);
            
    		// This method will be called by the parser each time there is a new value
    		Iterator pathIter = includes.listIterator();
    		while (pathIter.hasNext()) {
    			String path = (String) pathIter.next();
    			String convertedPath = convertPath(path);
    			// On MinGW, there is no facility for converting paths
    			if (convertedPath.startsWith("/")) continue;	//$NON-NLS-1$
    			// Add it if it is not a duplicate
    			if (!getIncludePaths().contains(convertedPath)){
    					getIncludePaths().add(convertedPath);
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
	}
	
	/* (non-Javadoc)
	 * Converts the argument from a POSIX-style path to a valid Win32 path. 
	 * If the string is already in the proper format it will not be changed.
	 * 
	 * @param path <code>String</code> containing path to convert 
	 * @return <code>String</code> containing the converted path 
	 */
	private String convertPath(String includePath) {
		// Convert a POSIX-style path to Win32
		String translatedPath = new CygpathTranslator(includePath).run();
		return translatedPath;
	}

}
