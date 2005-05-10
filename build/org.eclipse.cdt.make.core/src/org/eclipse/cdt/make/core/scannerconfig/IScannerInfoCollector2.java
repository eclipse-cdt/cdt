/***********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.make.core.scannerconfig;

import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * New scanner info collector interface - Eclipse dependent
 * 
 * @author vhirsl
 */
public interface IScannerInfoCollector2 extends IScannerInfoCollector {
	/** 
	 * @param project 
	 */
	public void setProject(IProject project);

    /**
	 * Relegate discovered scanner configuration to a scanner info provider 
	 * @param monitor
	 * @throws CoreException
	 */
	public void updateScannerConfiguration(IProgressMonitor monitor) throws CoreException;

    /**
     * Create and return new IDiscoveredPathInfo that can hopefully serialize
     * discovered scanner config to a file
     * 
     * @return pathInfo
     * @throws CoreException 
     */
    public IDiscoveredPathInfo createPathInfoObject();
    
}
