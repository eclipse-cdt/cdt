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
package org.eclipse.cdt.build.internal.core.scannerconfig.jobs;

import java.util.List;
import java.util.Properties;

import org.eclipse.cdt.build.core.scannerconfig.CfgInfoContext;
import org.eclipse.cdt.make.core.scannerconfig.IExternalScannerInfoProvider;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2;
import org.eclipse.cdt.make.internal.core.scannerconfig2.SCProfileInstance;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;

/**
 * Utility class for build and job related functionality
 *
 * @author vhirsl
 */
public class CfgSCJobsUtil {
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
    /*uncomment
    public static boolean getProviderScannerInfo(final IProject project,
            final IScannerConfigBuilderInfo2 buildInfo,
            final IProgressMonitor monitor) {
    	return getProviderScannerInfo(project, null, buildInfo, monitor);
    }
    */

    /**
     * Call ESI providers to get scanner info
     */
    public static SCProfileInstance getProviderScannerInfo(final IProject project,
    											 final CfgInfoContext context,
    											 SCProfileInstance profileInstance,
                                                 final IScannerConfigBuilderInfo2 buildInfo,
                                                 final Properties env,
                                                 final IProgressMonitor monitor) {
        final RC rc = new RC(false);
        // get the collector
        if(profileInstance == null){
        	profileInstance = ScannerConfigProfileManager.getInstance().
                getSCProfileInstance(project, context.toInfoContext(), buildInfo.getSelectedProfileId());
        }
        final IScannerInfoCollector collector = profileInstance.getScannerInfoCollector();

        List<String> providerIds = buildInfo.getProviderIdList();
        for (int i = 0; i < providerIds.size(); ++i) {
            final String providerId = providerIds.get(i);
            if (buildInfo.isProviderOutputParserEnabled(providerId)) {
                final IExternalScannerInfoProvider esiProvider = profileInstance.
                        createExternalScannerInfoProvider(providerId);
                if (esiProvider != null) {
                    ISafeRunnable runnable = new ISafeRunnable() {

                        @Override
						public void run() {
                            esiProvider.invokeProvider(monitor, project, context.toInfoContext(), providerId, buildInfo, collector, env);
                            rc.set(true);
                        }

                        @Override
						public void handleException(Throwable exception) {
                            rc.set(false);
                            ManagedBuilderCorePlugin.log(exception);
                        }

                    };
                    Platform.run(runnable);
                }
            }
        }
        if(rc.get())
        	return profileInstance;
        return null;
    }

    public static boolean updateScannerConfiguration(IProject project,
            IScannerConfigBuilderInfo2 buildInfo,
            final IProgressMonitor monitor) {
    	return updateScannerConfiguration(project, null, null, buildInfo, monitor);
    }

    /**
     * Update and persist scanner configuration
     */
    public static boolean updateScannerConfiguration(IProject project,
    												 CfgInfoContext context,
    												 SCProfileInstance profileInstance,
                                                     IScannerConfigBuilderInfo2 buildInfo,
                                                     final IProgressMonitor monitor) {
        final RC rc = new RC(false);
        // get the collector
//        if(context == null)
//        	context = ScannerConfigUtil.createContextForProject(project);

        if(profileInstance == null){
        	profileInstance = ScannerConfigProfileManager.getInstance().
                getSCProfileInstance(project, context.toInfoContext(), buildInfo.getSelectedProfileId());
        }
        IScannerInfoCollector collector = profileInstance.getScannerInfoCollector();
        if (collector instanceof IScannerInfoCollector2) {
            final IScannerInfoCollector2 collector2 = (IScannerInfoCollector2) collector;
            ISafeRunnable runnable = new ISafeRunnable() {

                @Override
				public void run() throws Exception {
                    collector2.updateScannerConfiguration(monitor);
                    rc.set(true);
                }

                @Override
				public void handleException(Throwable exception) {
                    rc.set(false);
                    ManagedBuilderCorePlugin.log(exception);
                }

            };
            Platform.run(runnable);
        }

        return rc.get();
    }

    public static SCProfileInstance readBuildOutputFile(final IProject project,
    		final CfgInfoContext context,
            final IScannerConfigBuilderInfo2 buildInfo,
            final Properties env,
            final IProgressMonitor monitor) {
        final RC rc = new RC(false);
        // get the collector
        SCProfileInstance profileInstance = ScannerConfigProfileManager.getInstance().
                getSCProfileInstance(project, context.toInfoContext(), buildInfo.getSelectedProfileId());
        final IScannerInfoCollector collector = profileInstance.getScannerInfoCollector();
        final IExternalScannerInfoProvider esiProvider = profileInstance.
                createBuildOutputProvider();

        if (buildInfo.isBuildOutputFileActionEnabled()) {
            ISafeRunnable runnable = new ISafeRunnable() {

                @Override
				public void run() {
                    esiProvider.invokeProvider(monitor, project, context.toInfoContext(), null, buildInfo, collector, env);
                    rc.set(true);
                }

                @Override
				public void handleException(Throwable exception) {
                    rc.set(false);
                    ManagedBuilderCorePlugin.log(exception);
                }

            };
            Platform.run(runnable);
        }

        if(rc.get())
        	return profileInstance;
        return null;

    }

    /**
     * @param project
     * @param buildInfo
     * @param monitor
     * @return
     */
    /*uncomment
    public static boolean readBuildOutputFile(final IProject project,
                                              final IScannerConfigBuilderInfo2 buildInfo,
                                              final IProgressMonitor monitor) {
    	return readBuildOutputFile(project, null, buildInfo, monitor);
    }
    */

}
