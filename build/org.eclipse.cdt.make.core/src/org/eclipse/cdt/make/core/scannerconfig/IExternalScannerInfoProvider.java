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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Interface for providers of C/C++ scanner info 
 * 
 * @author vhirsl
 */
public interface IExternalScannerInfoProvider {
	/**
	 * Invokes a C/C++ compiler with target specific options to generate
	 * compiler scanner info.
	 * 
	 * @param monitor
	 * @param current project - current project being built
	 * @param buildInfo - settings for ScannerConfigBuilder
	 * @param targetSpecificOptions - array of options affecting compiler specs
	 * @param collector - scanner info collector, for StdMake projects - ScannerInfoCollector
	 */
	public boolean invokeProvider(IProgressMonitor monitor, 
								  IProject currentProject,
								  IScannerConfigBuilderInfo buildInfo, 
								  List targetSpecificOptions,
								  IScannerInfoCollector collector); 
}
