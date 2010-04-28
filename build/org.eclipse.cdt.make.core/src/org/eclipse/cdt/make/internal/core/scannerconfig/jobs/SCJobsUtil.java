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
package org.eclipse.cdt.make.internal.core.scannerconfig.jobs;

import java.util.List;
import java.util.Properties;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariableManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IExternalScannerInfoProvider;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.internal.core.scannerconfig2.SCProfileInstance;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;

/**
 * Utility class for build and job related functionality
 * 
 * @author vhirsl
 */
public class SCJobsUtil {
    private static class RC {
        public RC(boolean init) {
            rc = init;
        }
        /**
         * @return Returns the rc.
         */
        public boolean get() {
            return rc;
        }
        /**
         * @param rc The rc to set.
         */
        public void set(boolean rc) {
            this.rc = rc;
        }
        
        @Override
		public String toString() {
            return rc ? "true" : "false"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        private boolean rc;
    }
    /**
     * Call ESI providers to get scanner info
     * 
     * @param collector 
     * @param buildInfo
     * @param monitor
     */
    public static boolean getProviderScannerInfo(final IProject project,
                                                 final IScannerConfigBuilderInfo2 buildInfo,
                                                 final IProgressMonitor monitor) {
    	return getProviderScannerInfo(project, buildInfo.getContext(), buildInfo, monitor);
    }
    	
    public static boolean getProviderScannerInfo(final IProject project,
    			final InfoContext context,
                final IScannerConfigBuilderInfo2 buildInfo,
                final IProgressMonitor monitor) {

        final RC rc = new RC(false);
        // get the collector
        SCProfileInstance profileInstance = ScannerConfigProfileManager.getInstance().
                getSCProfileInstance(project, context, buildInfo.getSelectedProfileId());
        final IScannerInfoCollector collector = profileInstance.getScannerInfoCollector();

        List<String> providerIds = buildInfo.getProviderIdList();
        for (int i = 0; i < providerIds.size(); ++i) {
            final String providerId = providerIds.get(i);
            if (buildInfo.isProviderOutputParserEnabled(providerId)) {
                final IExternalScannerInfoProvider esiProvider = profileInstance.
                        createExternalScannerInfoProvider(providerId);
                if (esiProvider != null) {
                    ISafeRunnable runnable = new ISafeRunnable() {

                        public void run() {
                        	// TODO we need the environment for the project here...
                        	ICProjectDescription projDesc = CoreModel.getDefault().getProjectDescription(project);
                        	ICConfigurationDescription configDesc = projDesc.getActiveConfiguration();
                        	IEnvironmentVariableManager envVarManager = CCorePlugin.getDefault().getBuildEnvironmentManager();
                        	IEnvironmentVariable[] envVars = envVarManager.getVariables(configDesc, true);
                        	Properties env = new Properties();
                        	for (int i = 0; i < envVars.length; ++i) {
                        		IEnvironmentVariable envVar = envVars[i];
                        		env.put(envVar.getName(), envVar.getValue());
                        	}
                            esiProvider.invokeProvider(monitor, project, context, providerId, buildInfo, collector, env);
                            rc.set(true);
                        }
            
                        public void handleException(Throwable exception) {
                            rc.set(false);
                            MakeCorePlugin.log(exception);
                        }
                        
                    };
                    SafeRunner.run(runnable);
                }
            }
        }
        return rc.get();
    }

    /**
     * Update and persist scanner configuration
     * 
     * @param project
     * @param buildInfo
     * @param monitor
     */
    public static boolean updateScannerConfiguration(IProject project,
                                                     IScannerConfigBuilderInfo2 buildInfo,
                                                     final IProgressMonitor monitor) {
    	return updateScannerConfiguration(project, buildInfo.getContext(), buildInfo, monitor);
    }

    /**
     * Update and persist scanner configuration
     * 
     * @param project
     * @param buildInfo
     * @param monitor
     */
    public static boolean updateScannerConfiguration(IProject project,
    												 InfoContext context,
                                                     IScannerConfigBuilderInfo2 buildInfo,
                                                     final IProgressMonitor monitor) {
        final RC rc = new RC(false);
        // get the collector
        SCProfileInstance profileInstance = ScannerConfigProfileManager.getInstance().
                getSCProfileInstance(project, context, buildInfo.getSelectedProfileId());
        IScannerInfoCollector collector = profileInstance.getScannerInfoCollector();
        if (collector instanceof IScannerInfoCollector2) {
            final IScannerInfoCollector2 collector2 = (IScannerInfoCollector2) collector;
            ISafeRunnable runnable = new ISafeRunnable() {

                public void run() throws Exception {
                    collector2.updateScannerConfiguration(monitor);
                    rc.set(true);
                }
                
                public void handleException(Throwable exception) {
                    rc.set(false);
                    MakeCorePlugin.log(exception);
                }

            };
            SafeRunner.run(runnable);
        }
        
        return rc.get();
    }

    /**
     * @param project
     * @param buildInfo
     * @param monitor
     * @return
     */
    public static boolean readBuildOutputFile(final IProject project,
                                              final IScannerConfigBuilderInfo2 buildInfo,
                                              final IProgressMonitor monitor) {
    	return readBuildOutputFile(project, buildInfo.getContext(), buildInfo, monitor);
    }

    /**
     * @param project
     * @param buildInfo
     * @param monitor
     * @return
     */
    public static boolean readBuildOutputFile(final IProject project,
    										  final InfoContext context,
                                              final IScannerConfigBuilderInfo2 buildInfo,
                                              final IProgressMonitor monitor) {
        final RC rc = new RC(false);
        // get the collector
        SCProfileInstance profileInstance = ScannerConfigProfileManager.getInstance().
                getSCProfileInstance(project, context, buildInfo.getSelectedProfileId());
        final IScannerInfoCollector collector = profileInstance.getScannerInfoCollector();
        final IExternalScannerInfoProvider esiProvider = profileInstance.
                createBuildOutputProvider();

        if (buildInfo.isBuildOutputFileActionEnabled()) {
            ISafeRunnable runnable = new ISafeRunnable() {
                
                public void run() {
                    esiProvider.invokeProvider(monitor, project, context, null, buildInfo, collector, null);
                    rc.set(true);
                }
        
                public void handleException(Throwable exception) {
                    rc.set(false);
                    MakeCorePlugin.log(exception);
                }
                
            };
            SafeRunner.run(runnable);
        }
        
        return rc.get();
    }

}
