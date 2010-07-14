/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.scannerconfig;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.CygpathTranslator;
import org.eclipse.core.resources.IProject;

/**
 * Implementation class for gathering the built-in compiler settings for 
 * Cygwin-based targets.
 * 
 * @since 2.0
 */
public class DefaultGnuWinScannerInfoCollector extends DefaultGCCScannerInfoCollector {
	private IProject fProject; 
    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector#contributeToScannerConfig(java.lang.Object, java.util.Map)
     */
    @Override
    public void contributeToScannerConfig(Object resource, Map scannerInfo) {
        // check the resource
//        if (resource != null && resource instanceof IResource &&
//                ((IResource) resource).getProject().equals(getProject())) {
            List includes = (List) scannerInfo.get(ScannerInfoTypes.INCLUDE_PATHS);
//            List symbols = (List) scannerInfo.get(ScannerInfoTypes.SYMBOL_DEFINITIONS);
            
    		// This method will be called by the parser each time there is a new value
            List translatedIncludes = CygpathTranslator.translateIncludePaths(fProject, includes);
    		Iterator pathIter = translatedIncludes.listIterator();
    		while (pathIter.hasNext()) {
    			String convertedPath = (String) pathIter.next();
    			// On MinGW, there is no facility for converting paths
    			if (convertedPath.startsWith("/")) //$NON-NLS-1$
    				pathIter.remove();
//    			// Add it if it is not a duplicate
//    			if (!getIncludePaths().contains(convertedPath)){
//    					getIncludePaths().add(convertedPath);
//    			}
    		}
    		scannerInfo.put(ScannerInfoTypes.INCLUDE_PATHS, translatedIncludes);
    		
//    		// Now add the macros
//    		Iterator symbolIter = symbols.listIterator();
//    		while (symbolIter.hasNext()) {
//    			// See if it has an equals
//    			String[] macroTokens = ((String)symbolIter.next()).split(EQUALS);
//    			String macro = macroTokens[0].trim();
//    			String value = (macroTokens.length > 1) ? macroTokens[1].trim() : new String();
//    			getDefinedSymbols().put(macro, value);
//    		}
    		super.contributeToScannerConfig(resource, scannerInfo);
        }
//	}
	@Override
	public void setProject(IProject project) {
		fProject = project;
		super.setProject(project);
	}

	@Override
	public void setInfoContext(InfoContext context) {
		fProject = context.getProject();
		super.setInfoContext(context);
	}
}
