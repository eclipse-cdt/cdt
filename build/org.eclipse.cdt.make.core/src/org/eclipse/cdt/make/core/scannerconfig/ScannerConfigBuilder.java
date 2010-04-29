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

import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.resources.ACBuilder;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.cdt.make.internal.core.scannerconfig.jobs.SCJobsUtil;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * Runs after standard make builder.
 * Consolidates discovered scanner configuration and updates project's scanner configuration.
 * 
 * @deprecated as of CDT 4.0. Used by legacy CDT 3.X projects.
 * Replaced by ScannerConfigBuilder in org.eclipse.cdt.managedbuilder.core.
 * 
 * @see IncrementalProjectBuilder
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@Deprecated
public class ScannerConfigBuilder extends ACBuilder {
	public final static String BUILDER_ID = MakeCorePlugin.getUniqueIdentifier() + ".ScannerConfigBuilder"; //$NON-NLS-1$

	public ScannerConfigBuilder() {
		super();
	}

	/**
	 * @see IncrementalProjectBuilder#build
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected IProject [] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		if (DEBUG_EVENTS)
			printEvent(kind, args);

		// If auto discovery is disabled, do nothing
//		boolean autodiscoveryEnabled;
		if(buildNewStyle(getProject(), monitor))
			return getProject().getReferencedProjects();
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
//			autodiscoveryEnabled = false;
			autodiscoveryEnabled2 = false;
            MakeCorePlugin.log(e);
		}
		return getProject().getReferencedProjects();
	}
	
	protected boolean buildNewStyle(IProject project, IProgressMonitor monitor) throws CoreException{
		ICProjectDescription des = CCorePlugin.getDefault().getProjectDescription(project, false);
		if(!CCorePlugin.getDefault().isNewStyleProject(des))
			return false;
		
		ICConfigurationDescription[] cfgs = des.getConfigurations();
		IScannerConfigBuilderInfo2Set container = ScannerConfigProfileManager.createScannerConfigBuildInfo2Set(project);
		monitor.beginTask(MakeMessages.getString("ScannerConfigBuilder.0"), cfgs.length + 1); //$NON-NLS-1$
		boolean wasbuilt = false;
		for(int i = 0; i < cfgs.length; i++){
			ICConfigurationDescription cfg = cfgs[i];
			CConfigurationData data = cfg.getConfigurationData();
			InfoContext context = new InfoContext(project, data.getId());
			IScannerConfigBuilderInfo2 info = container.getInfo(context);
			if(info == null){
//				context = new InfoContext(project);
				info = container.getInfo(new InfoContext(project));
			}

			if(build(project, context, info, new SubProgressMonitor(monitor, 1)))
				wasbuilt = true;
		}
		
		if(wasbuilt)
			CCorePlugin.getDefault().updateProjectDescriptions(new IProject[]{project}, new SubProgressMonitor(monitor, 1));
		
		monitor.done();
		return true;
	}
	
	protected boolean build(IProject project, InfoContext context, IScannerConfigBuilderInfo2 buildInfo2, IProgressMonitor monitor){
            boolean autodiscoveryEnabled2 = buildInfo2.isAutoDiscoveryEnabled();

            if (autodiscoveryEnabled2) {
                monitor.beginTask(MakeMessages.getString("ScannerConfigBuilder.Invoking_Builder"), 100); //$NON-NLS-1$
                monitor.subTask(MakeMessages.getString("ScannerConfigBuilder.Invoking_Builder") +   //$NON-NLS-1$ 
                        getProject().getName());
                
                // get scanner info from all external providers
                SCJobsUtil.getProviderScannerInfo(getProject(), context, buildInfo2, new SubProgressMonitor(monitor, 70));

                // update and persist scanner configuration
                SCJobsUtil.updateScannerConfiguration(getProject(), context, buildInfo2, new SubProgressMonitor(monitor, 30));
                return true;
            }
            return false;
	}
	
	

}
