/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.core.scannerconfig;

import java.util.List;
import java.util.Map;

/**
 * Interface for scanner info collector.
 * Used by scanner info console parsers.
 * Eclipse independent.
 * 
 * @author vhirsl
 */
public interface IScannerInfoCollector {
	/**
	 * Contribute to resource's scanner configuration
	 * 
	 * @param resource - if used from within Eclipse it is expected that resource is a
	 * member of <code>org.eclipse.core.resources.IResource</code> hierarchy. 
	 * If used outside of Eclipse then resource is expected to be a 
	 * <code>java.io.File<code> type.
	 * @param scannerInfo - a map of key - list pairs, where key is the type of extra info
	 * i.e. target specific options or imacros commands,...
	 */
	public void contributeToScannerConfig(Object resource, Map scannerInfo);

    /**
     * Returns specific piece of discovered scanner info for a resource
     * discovered during the last collection cycle 
     * @param type
     * @param resource
     * @return
     */
    public List getCollectedScannerInfo(Object resource, ScannerInfoTypes type);

}
