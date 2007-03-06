/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.build.core.scannerconfig;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.eclipse.cdt.build.internal.core.scannerconfig.CfgDiscoveredPathManager;
import org.eclipse.cdt.build.internal.core.scannerconfig.jobs.CfgSCJobsUtil;
import org.eclipse.cdt.build.internal.core.scannerconfig2.CfgScannerConfigProfileManager;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariableManager;
import org.eclipse.cdt.core.resources.ACBuilder;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.internal.core.scannerconfig2.SCProfileInstance;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.newmake.internal.core.MakeMessages;
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
	/*
	 * calculation flags
	 */
	/**
	 * tells the discovery mechanism to perform core settings update
	 */
	public static final int PERFORM_CORE_UPDATE = 1;
	
	/**
	 * force the discovery, i.e. run the discovery even if it is disabled
	 */
	public static final int FORCE_DISCOVERY = 1 << 1;

	public final static String BUILDER_ID = ManagedBuilderCorePlugin.getUniqueIdentifier() + ".ScannerConfigBuilder"; //$NON-NLS-1$

	public ScannerConfigBuilder() {
		super();
	}

	/**
	 * @see IncrementalProjectBuilder#build
	 */
	protected IProject [] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		// If auto discovery is disabled, do nothing
//		boolean autodiscoveryEnabled;
//		boolean autodiscoveryEnabled2;
//		IScannerConfigBuilderInfo2 buildInfo2 = null;
//		IConfiguration cfg = ScannerConfigUtil.getActiveConfiguration(getProject());
		IManagedBuildInfo bInfo = ManagedBuildManager.getBuildInfo(getProject());
		if(bInfo != null){
			IConfiguration cfgs[] = bInfo.getManagedProject().getConfigurations();
			if(cfgs.length != 0){
				monitor.beginTask(MakeMessages.getString("ScannerConfigBuilder.Invoking_Builder"), cfgs.length);
				for(int i = 0; i < cfgs.length; i++){
					build(cfgs[i], 0, new SubProgressMonitor(monitor, 1));
				}
			}
			
			CfgDiscoveredPathManager.getInstance().updateCoreSettings(getProject(), cfgs);
		}
		
		
		return getProject().getReferencedProjects();
	}
	
	public static void build(IConfiguration cfg, int flags, IProgressMonitor monitor){
		if(cfg != null){
			//			IScannerConfigBuilderInfo buildInfo = MakeCorePlugin.createScannerConfigBuildInfo(getProject(), BUILDER_ID);
			//			autodiscoveryEnabled = buildInfo.isAutoDiscoveryEnabled();
			//			
			//            if (autodiscoveryEnabled) {
			//                monitor.beginTask("ScannerConfigBuilder.Invoking_Builder", 100); //$NON-NLS-1$
			//                monitor.subTask(MakeMessages.getString("ScannerConfigBuilder.Invoking_Builder") +   //$NON-NLS-1$ 
			//                        getProject().getName());
			//                ScannerInfoCollector.getInstance().updateScannerConfiguration(getProject(), new SubProgressMonitor(monitor, 100));
			//            }
						ICfgScannerConfigBuilderInfo2Set info = CfgScannerConfigProfileManager.getCfgScannerConfigBuildInfo(cfg);
						IProject project = cfg.getOwner().getProject();
						Map infoMap = info.getInfoMap();
						int num = infoMap.size();
						if(num != 0){
							Properties envProps = calcEnvironment(cfg);
							monitor.beginTask(MakeMessages.getString("ScannerConfigBuilder.Invoking_Builder"), num);
							for(Iterator iter = infoMap.entrySet().iterator(); iter.hasNext();){
								try {
									Map.Entry entry = (Map.Entry)iter.next();
									CfgInfoContext c = (CfgInfoContext)entry.getKey();
									IScannerConfigBuilderInfo2 buildInfo2 = (IScannerConfigBuilderInfo2)entry.getValue();
									build(c, buildInfo2, (flags & (~PERFORM_CORE_UPDATE)), envProps, new SubProgressMonitor(monitor, 1));
								} catch (CoreException e){
									// builder not installed or disabled
									//			autodiscoveryEnabled = false;
		//										autodiscoveryEnabled2 = false;
									ManagedBuilderCorePlugin.log(e);
								}
							}
						}
						if((flags & PERFORM_CORE_UPDATE) != 0)
							CfgDiscoveredPathManager.getInstance().updateCoreSettings(project, new IConfiguration[]{cfg});
				}

	}
	
	private static Properties calcEnvironment(IConfiguration cfg){
		Properties envProps = new Properties();
		ICConfigurationDescription cfgDes = ManagedBuildManager.getDescriptionForConfiguration(cfg);
		IEnvironmentVariableManager mngr = CCorePlugin.getDefault().getBuildEnvironmentManager();
		IEnvironmentVariable[] vars = mngr.getVariables(cfgDes, true);
		for(int i = 0; i < vars.length; i++){
			envProps.setProperty(vars[i].getName(), vars[i].getValue());
		}
		
		return envProps;
	}
	
	public static SCProfileInstance build(CfgInfoContext context, IScannerConfigBuilderInfo2 buildInfo2, int flags, Properties env, IProgressMonitor monitor) throws CoreException{
		IConfiguration cfg = context.getConfiguration();
		IProject project = cfg.getOwner().getProject();
        boolean autodiscoveryEnabled2 = buildInfo2.isAutoDiscoveryEnabled();

        if (autodiscoveryEnabled2 || ((flags & FORCE_DISCOVERY) != 0)) {
            monitor.beginTask(MakeMessages.getString("ScannerConfigBuilder.Invoking_Builder"), 100); //$NON-NLS-1$
            monitor.subTask(MakeMessages.getString("ScannerConfigBuilder.Invoking_Builder") +   //$NON-NLS-1$ 
                    project.getName());
            
            if(env == null)
            	env = calcEnvironment(cfg);
            
            // get scanner info from all external providers
            SCProfileInstance instance = CfgSCJobsUtil.getProviderScannerInfo(project, context, null, buildInfo2, env, new SubProgressMonitor(monitor, 70));

            // update and persist scanner configuration
            CfgSCJobsUtil.updateScannerConfiguration(project, context, instance, buildInfo2, new SubProgressMonitor(monitor, 30));
            
			if((flags & PERFORM_CORE_UPDATE) != 0)
				CfgDiscoveredPathManager.getInstance().updateCoreSettings(project, new IConfiguration[]{cfg});

			return instance;
        }
        
        return null;
	}
}
