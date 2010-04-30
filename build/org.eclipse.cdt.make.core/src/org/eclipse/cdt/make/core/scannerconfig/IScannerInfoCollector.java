/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
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
@SuppressWarnings("rawtypes")
public interface IScannerInfoCollector {
	/**
	 * Contribute to resource's scanner configuration
	 * 
	 * @param resource
	 *    <li> {@link org.eclipse.core.resources.IResource} if used from within Eclipse.</li>
	 *    <li> {@link java.io.File}  if used outside of Eclipse.</li>
	 *    <li> {@code Integer} if represents command ID.</li>
	 * @param scannerInfo - a map of key - list pairs, where key is the type of extra info
	 * i.e. target specific options or imacros commands, for example
	 *    <li>{@code Map<ScannerInfoTypes, List<String>>}</li>
	 *    <li>{@code Map<ScannerInfoTypes, List<CCommandDSC>>}</li>
	 */
	public void contributeToScannerConfig(Object resource, Map scannerInfo);

    /**
     * @return specific piece of discovered scanner info for a resource
     * discovered during the last collection cycle, can be:
	 *    <li>{@code List<String>}</li>
	 *    <li>{@code List<CCommandDSC>}</li>
     */
    public List getCollectedScannerInfo(Object resource, ScannerInfoTypes type);

}
