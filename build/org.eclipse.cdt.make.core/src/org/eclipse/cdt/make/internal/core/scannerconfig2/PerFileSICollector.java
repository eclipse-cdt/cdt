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
package org.eclipse.cdt.make.internal.core.scannerconfig2;

import java.util.List;
import java.util.Map;

import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2;
import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * TODO Provide description
 * 
 * @author vhirsl
 */
public class PerFileSICollector implements IScannerInfoCollector2 {

    /**
     * 
     */
    public PerFileSICollector() {
        super();
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2#setProject(org.eclipse.core.resources.IProject)
     */
    public void setProject(IProject project) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2#updateScannerConfiguration(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void updateScannerConfiguration(IProgressMonitor monitor)
            throws CoreException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector#contributeToScannerConfig(java.lang.Object, java.util.Map)
     */
    public void contributeToScannerConfig(Object resource, Map scannerInfo) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector#getCollectedScannerInfo(java.lang.Object, org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes)
     */
    public List getCollectedScannerInfo(Object resource, ScannerInfoTypes type) {
        // TODO Auto-generated method stub
        return null;
    }

}
