/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Doug Schaefer - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.build.crossgcc;

import java.util.Map;

import org.eclipse.cdt.build.core.scannerconfig.CfgInfoContext;
import org.eclipse.cdt.build.core.scannerconfig.ICfgScannerConfigBuilderInfo2Set;
import org.eclipse.cdt.build.internal.core.scannerconfig.CfgDiscoveredPathManager;
import org.eclipse.cdt.build.internal.core.scannerconfig2.CfgScannerConfigProfileManager;
import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.ProcessRunner;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollectorCleaner;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredPathInfo;
import org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredScannerInfoStore;
import org.eclipse.cdt.make.internal.core.scannerconfig2.SCProfileInstance;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class SetCrossCommandProcess extends ProcessRunner {

	@Override
	public void process(TemplateCore template, ProcessArgument[] args,
			String processId, IProgressMonitor monitor)
			throws ProcessFailureException {
		String projectName = args[0].getSimpleValue();
		String prefix = args[1].getSimpleValue();
		String path = args[2].getSimpleValue();
		
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (!project.exists())
			return;
		
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		if (buildInfo == null)
			return;
		
		IConfiguration[] configs = buildInfo.getManagedProject().getConfigurations();
		for (IConfiguration config : configs) {
			IToolChain toolchain = config.getToolChain();
			IOption option = toolchain.getOptionBySuperClassId("cdt.managedbuild.option.gnu.cross.prefix");
			ManagedBuildManager.setOption(config, toolchain, option, prefix);
			option = toolchain.getOptionBySuperClassId("cdt.managedbuild.option.gnu.cross.path");
			ManagedBuildManager.setOption(config, toolchain, option, path);
			
			ICfgScannerConfigBuilderInfo2Set cbi = CfgScannerConfigProfileManager.getCfgScannerConfigBuildInfo(config);
			Map<CfgInfoContext, IScannerConfigBuilderInfo2> map = cbi.getInfoMap();
			for (CfgInfoContext cfgInfoContext : map.keySet()) {
				IScannerConfigBuilderInfo2 bi = map.get(cfgInfoContext);
				String providerId = "specsFile";
				String runCommand = bi.getProviderRunCommand(providerId);
				bi.setProviderRunCommand(providerId, prefix + runCommand);
				try {
					bi.save();
				} catch (CoreException e) {
					throw new ProcessFailureException(e);
				}
				
				// Clear the path info that was captured at project creation time
				// TODO we need an API to do this to avoid the discouraged access warnings.
				
				DiscoveredPathInfo pathInfo = new DiscoveredPathInfo(project);
				InfoContext infoContext = cfgInfoContext.toInfoContext();
				
				// 1. Remove scanner info from .metadata/.plugins/org.eclipse.cdt.make.core/Project.sc
				DiscoveredScannerInfoStore dsiStore = DiscoveredScannerInfoStore.getInstance();
				try {
					dsiStore.saveDiscoveredScannerInfoToState(project, infoContext, pathInfo);
				} catch (CoreException e) {
					e.printStackTrace();
				}
				
				// 2. Remove scanner info from CfgDiscoveredPathManager cache and from the Tool
				CfgDiscoveredPathManager cdpManager = CfgDiscoveredPathManager.getInstance();
				cdpManager.removeDiscoveredInfo(project, cfgInfoContext);

				// 3. Remove scanner info from SI collector
				IScannerConfigBuilderInfo2 buildInfo2 = map.get(cfgInfoContext);
				if (buildInfo2!=null) {
					ScannerConfigProfileManager scpManager = ScannerConfigProfileManager.getInstance();
					String selectedProfileId = buildInfo2.getSelectedProfileId();
					SCProfileInstance profileInstance = scpManager.getSCProfileInstance(project, infoContext, selectedProfileId);
					
					IScannerInfoCollector collector = profileInstance.getScannerInfoCollector();
					if (collector instanceof IScannerInfoCollectorCleaner) {
						((IScannerInfoCollectorCleaner) collector).deleteAll(project);
					}
					buildInfo2 = null;
				}
			}
		}
		
		ManagedBuildManager.saveBuildInfo(project, true);
	}
	
}
