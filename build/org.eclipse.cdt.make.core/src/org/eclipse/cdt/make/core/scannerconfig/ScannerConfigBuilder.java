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

import java.util.Map;

import org.eclipse.cdt.core.resources.ACBuilder;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.cdt.make.internal.core.scannerconfig.jobs.SCJobsUtil;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * Runs after standard make builder.
 * Consolidates discovered scanner configuration and updates project's scanner configuration.
 * 
 * @see IncrementalProjectBuilder
 */
public class ScannerConfigBuilder extends ACBuilder {
	public final static String BUILDER_ID = MakeCorePlugin.getUniqueIdentifier() + ".ScannerConfigBuilder"; //$NON-NLS-1$

	public ScannerConfigBuilder() {
		super();
	}

	/**
	 * @see IncrementalProjectBuilder#build
	 */
	protected IProject [] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		// If auto discovery is disabled, do nothing
		boolean autodiscoveryEnabled;
		boolean autodiscoveryEnabled2;
		IScannerConfigBuilderInfo2 buildInfo2 = null;
		try {
//			IScannerConfigBuilderInfo buildInfo = MakeCorePlugin.createScannerConfigBuildInfo(getProject(), BUILDER_ID);
//			autodiscoveryEnabled = buildInfo.isAutoDiscoveryEnabled();
//			
//            if (autodiscoveryEnabled) {
//                monitor.beginTask("ScannerConfigBuilder.Invoking_Builder", 100); //$NON-NLS-1$
//                monitor.subTask(MakeMessages.getString("ScannerConfigBuilder.Invoking_Builder") +   //$NON-NLS-1$ 
//                        getProject().getName());
//                ScannerInfoCollector.getInstance().updateScannerConfiguration(getProject(), new SubProgressMonitor(monitor, 100));
//            }
            
            buildInfo2 = ScannerConfigProfileManager.createScannerConfigBuildInfo2(getProject());
            autodiscoveryEnabled2 = buildInfo2.isAutoDiscoveryEnabled();

            if (autodiscoveryEnabled2) {
                monitor.beginTask(MakeMessages.getString("ScannerConfigBuilder.Invoking_Builder"), 100); //$NON-NLS-1$
                monitor.subTask(MakeMessages.getString("ScannerConfigBuilder.Invoking_Builder") +   //$NON-NLS-1$ 
                        getProject().getName());
                
                // get scanner info from all external providers
                SCJobsUtil.getProviderScannerInfo(getProject(), buildInfo2, new SubProgressMonitor(monitor, 70));

                // update and persist scanner configuration
                SCJobsUtil.updateScannerConfiguration(getProject(), buildInfo2, new SubProgressMonitor(monitor, 30));
            }
		} 
		catch (CoreException e) {
			// builder not installed or disabled
			autodiscoveryEnabled = false;
			autodiscoveryEnabled2 = false;
            MakeCorePlugin.log(e);
		}
		
		return getProject().getReferencedProjects();
	}

}
