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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2;
import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Implementation class for gathering the built-in compiler settings for 
 * GCC-based targets. The assumption is that the tools will answer path 
 * information in POSIX format and that the Scanner will be able to search for 
 * files using this format.
 * 
 * @since 2.0
 */
public class DefaultGCCScannerInfoCollector implements IScannerInfoCollector2 {
	protected Map definedSymbols;
	protected static final String EQUALS = "=";	//$NON-NLS-1$
	protected List includePaths;
	protected IProject project;
	
	/**
	 * 
	 */
	public DefaultGCCScannerInfoCollector() {
		super();
	}

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
				getIncludePaths().add(path);
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
	 * @see org.eclipse.cdt.core.parser.IScannerInfo#getDefinedSymbols()
	 */
	public Map getDefinedSymbols() {
		if (definedSymbols == null) {
			definedSymbols = new HashMap();
		}
		return definedSymbols;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.scannerconfig.IManagedScannerInfoCollector#getIncludePaths()
	 */
	public List getIncludePaths() {
		if (includePaths == null) {
			includePaths = new ArrayList();
		}
		return includePaths;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.scannerconfig.IManagedScannerInfoCollector#setProject(org.eclipse.core.resources.IProject)
	 */
	public void setProject(IProject project) {
		this.project = project;
		
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2#updateScannerConfiguration(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void updateScannerConfiguration(IProgressMonitor monitor) throws CoreException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector#getCollectedScannerInfo(java.lang.Object, org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes)
     */
    public List getCollectedScannerInfo(Object resource, ScannerInfoTypes type) {
        List rv = null;
        String errorMessage = null;
        if (resource == null) {
            errorMessage = "resource is null";//$NON-NLS-1$
        } 
        else if (!(resource instanceof IResource)) {
            errorMessage = "resource is not an IResource";//$NON-NLS-1$
        }
        else if (((IResource) resource).getProject() == null) {
            errorMessage = "project is null";//$NON-NLS-1$
        }
        else if (((IResource) resource).getProject() != project) {
            errorMessage = "wrong project";//$NON-NLS-1$
        }
        
        if (errorMessage != null) {
            TraceUtil.outputError("DefaultGCCScannerInfoCollector.getCollectedScannerInfo : ", errorMessage); //$NON-NLS-1$
        }
        else if (type.equals(ScannerInfoTypes.INCLUDE_PATHS)) {
            rv = getIncludePaths();
        }
        else if (type.equals(ScannerInfoTypes.SYMBOL_DEFINITIONS)) {
            rv = new ArrayList();
            Map symbols = getDefinedSymbols();
            for (Iterator i = symbols.keySet().iterator(); i.hasNext(); ) {
                String macro = (String) i.next();
                String value = (String) symbols.get(macro);
                if (value.length() > 0) {
                    rv.add(macro + EQUALS + value);
                }
                else {
                    rv.add(macro);
                }
            }
        }
        else {
            rv = new ArrayList();
        }
        return rv;
    }

}
