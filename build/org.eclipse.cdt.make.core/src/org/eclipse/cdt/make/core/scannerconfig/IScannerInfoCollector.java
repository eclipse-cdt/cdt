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
package org.eclipse.cdt.make.core.scannerconfig;

import java.util.List;

import org.eclipse.core.resources.IResource;

/**
 * Interface for scanner info collector.
 * Used by scanner info console parsers.
 * 
 * @author vhirsl
 */
public interface IScannerInfoCollector {
	/**
	 * Contribute to resource's scanner configuration
	 * 
	 * @param resource
	 * @param includes
	 * @param symbols
	 * @param targetSpecificOptions
	 */
	public void contributeToScannerConfig(IResource resource,
										  List includes,
										  List symbols,
										  List targetSpecificOptions);
}