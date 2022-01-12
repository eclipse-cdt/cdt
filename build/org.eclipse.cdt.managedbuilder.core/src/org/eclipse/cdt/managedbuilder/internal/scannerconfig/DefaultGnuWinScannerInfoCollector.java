/*******************************************************************************
 * Copyright (c) 2004, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
	@SuppressWarnings("unchecked")
	@Override
	public void contributeToScannerConfig(Object resource, @SuppressWarnings("rawtypes") Map scannerInfo) {
		// check the resource
		//        if (resource != null && resource instanceof IResource &&
		//                ((IResource) resource).getProject().equals(getProject())) {
		List<String> includes = (List<String>) scannerInfo.get(ScannerInfoTypes.INCLUDE_PATHS);
		//            List symbols = (List) scannerInfo.get(ScannerInfoTypes.SYMBOL_DEFINITIONS);

		// This method will be called by the parser each time there is a new value
		List<String> translatedIncludes = CygpathTranslator.translateIncludePaths(fProject, includes);
		Iterator<String> pathIter = translatedIncludes.listIterator();
		while (pathIter.hasNext()) {
			String convertedPath = pathIter.next();
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
		//    			String value = (macroTokens.length > 1) ? macroTokens[1].trim() : ""; //$NON-NLS-1$
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
