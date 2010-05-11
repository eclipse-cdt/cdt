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
package org.eclipse.cdt.build.core.scannerconfig;

import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.eclipse.cdt.build.internal.core.scannerconfig.CfgDiscoveredPathManager;
import org.eclipse.cdt.build.internal.core.scannerconfig.jobs.CfgSCJobsUtil;
import org.eclipse.cdt.build.internal.core.scannerconfig2.CfgScannerConfigProfileManager;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariableManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.resources.ACBuilder;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.internal.core.scannerconfig2.SCProfileInstance;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager;
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
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
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
	/**
	 * skip running gcc to fetch built-in specs scanner info
	 * @since 7.0
	 */
	public static final int SKIP_SI_DISCOVERY = 1 << 2;

	public final static String BUILDER_ID = ManagedBuilderCorePlugin.getUniqueIdentifier() + ".ScannerConfigBuilder"; //$NON-NLS-1$
	
	public ScannerConfigBuilder() {
		super();
	}

	/**
	 * @see IncrementalProjectBuilder#build
	 */
	@Override
	protected IProject [] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		if (DEBUG_EVENTS)
			printEvent(kind, args);

		// If auto discovery is disabled, do nothing
//		boolean autodiscoveryEnabled;
//		boolean autodiscoveryEnabled2;
//		IScannerConfigBuilderInfo2 buildInfo2 = null;
//		IConfiguration cfg = ScannerConfigUtil.getActiveConfiguration(getProject());
		IManagedBuildInfo bInfo = ManagedBuildManager.getBuildInfo(getProject());
		if(bInfo != null){
			IConfiguration cfgs[] = bInfo.getManagedProject().getConfigurations();
			if(cfgs.length != 0){
				if(!needAllConfigBuild()){
					ICProjectDescription des = CoreModel.getDefault().getProjectDescription(getProject(), false);
					IConfiguration cfg = null;
					if(des != null){
						ICConfigurationDescription settingCfgDes = des.getDefaultSettingConfiguration();
						if(settingCfgDes != null){
							for(int i = 0; i < cfgs.length; i++){
								if(settingCfgDes.getId().equals(cfgs[i].getId())){
									cfg = cfgs[i];
									break;
								}
							}
						}
					}
					if(cfg != null){
						cfgs = new IConfiguration[]{cfg};
					} else {
						cfgs = new IConfiguration[0];
					}
				}
				int numWork = cfgs.length;
				if(numWork > 0){
					monitor.beginTask(MakeMessages.getString("ScannerConfigBuilder.Invoking_Builder"), numWork); //$NON-NLS-1$
					for(int i = 0; i < cfgs.length; i++){
						build(cfgs[i], 0, new SubProgressMonitor(monitor, 1));
					}
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
						Map<CfgInfoContext, IScannerConfigBuilderInfo2> infoMap = info.getInfoMap();
						int num = infoMap.size();
						if(num != 0){
							Properties envProps = calcEnvironment(cfg);
							monitor.beginTask(MakeMessages.getString("ScannerConfigBuilder.Invoking_Builder"), num); //$NON-NLS-1$
							for (Entry<CfgInfoContext, IScannerConfigBuilderInfo2> entry : infoMap.entrySet()) {
								try {
									CfgInfoContext c = entry.getKey();
									IScannerConfigBuilderInfo2 buildInfo2 = entry.getValue();
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
            monitor.subTask(MakeMessages.getString("ScannerConfigBuilder.Invoking_Builder") + //$NON-NLS-1$
                    project.getName());
            
            if(env == null)
            	env = calcEnvironment(cfg);
            
            // get scanner info from all external providers
            SCProfileInstance instance = ScannerConfigProfileManager.getInstance().
        		getSCProfileInstance(project, context.toInfoContext(), buildInfo2.getSelectedProfileId());
            // if there are any providers call job to pull scanner info
            if ((flags & SKIP_SI_DISCOVERY) == 0) {
                if ((instance == null) || !buildInfo2.getProviderIdList().isEmpty())
                    instance = CfgSCJobsUtil.getProviderScannerInfo(project, context, instance, buildInfo2, env, new SubProgressMonitor(monitor, 70));
            }

            // update and persist scanner configuration
            CfgSCJobsUtil.updateScannerConfiguration(project, context, instance, buildInfo2, new SubProgressMonitor(monitor, 30));
            
            // this erroneously removes the infor right after it gets created... bad
            //CfgDiscoveredPathManager.getInstance().removeDiscoveredInfo(project, context, false);
            
			if((flags & PERFORM_CORE_UPDATE) != 0)
				CfgDiscoveredPathManager.getInstance().updateCoreSettings(project, new IConfiguration[]{cfg});

			return instance;
        }
        
        return null;
	}
}
