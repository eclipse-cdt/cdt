/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
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

	public void setProject(IProject project);

	/**
	 * Relegate discovered scanner configuration to a scanner info provider
	 */
	public void updateScannerConfiguration(IProgressMonitor monitor) throws CoreException;

	/**
	 * Create and return new IDiscoveredPathInfo that can hopefully serialize
	 * discovered scanner config to a file
	 */
	public IDiscoveredPathInfo createPathInfoObject();

}
