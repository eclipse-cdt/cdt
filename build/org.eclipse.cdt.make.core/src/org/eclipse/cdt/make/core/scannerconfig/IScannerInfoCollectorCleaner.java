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

import org.eclipse.core.resources.IResource;

/**
 * Utility functions some collectors may need
 * 
 * @author vhirsl
 */
public interface IScannerInfoCollectorCleaner {
    /**
     * Delete all discovered paths for a resource
     * 
     * @param project
     */
    public void deleteAllPaths(IResource resource);

    /**
     * Delete all discovered symbols for a resource
     * 
     * @param project
     */
    public void deleteAllSymbols(IResource resource);

    /**
     * Delete a specific include path
     * 
     * @param resource
     * @param path
     */
    public void deletePath(IResource resource, String path);

    /**
     * Delete a specific symbol definition
     * 
     * @param resource
     * @param path
     */
    public void deleteSymbol(IResource resource, String symbol);
    
}
