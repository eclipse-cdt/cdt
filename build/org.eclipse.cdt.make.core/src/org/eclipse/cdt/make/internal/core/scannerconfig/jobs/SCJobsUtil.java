/***********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig.jobs;

import java.util.List;

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IExternalScannerInfoProvider;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2;
import org.eclipse.cdt.make.internal.core.scannerconfig2.SCProfileInstance;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;

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
        final RC rc = new RC(false);
        // get the collector
        SCProfileInstance profileInstance = ScannerConfigProfileManager.getInstance().
                getSCProfileInstance(project, buildInfo.getSelectedProfileId());
        final IScannerInfoCollector collector = profileInstance.getScannerInfoCollector();

        List providerIds = buildInfo.getProviderIdList();
        for (int i = 0; i < providerIds.size(); ++i) {
            final String providerId = (String) providerIds.get(i);
            if (buildInfo.isProviderOutputParserEnabled(providerId)) {
                final IExternalScannerInfoProvider esiProvider = profileInstance.
                        createExternalScannerInfoProvider(providerId);
                if (esiProvider != null) {
                    ISafeRunnable runnable = new ISafeRunnable() {

                        public void run() {
                            esiProvider.invokeProvider(monitor, project, providerId, buildInfo, collector);
                            rc.set(true);
                        }
            
                        public void handleException(Throwable exception) {
                            rc.set(false);
                            MakeCorePlugin.log(exception);
                        }
                        
                    };
                    Platform.run(runnable);
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
        final RC rc = new RC(false);
        // get the collector
        SCProfileInstance profileInstance = ScannerConfigProfileManager.getInstance().
                getSCProfileInstance(project, buildInfo.getSelectedProfileId());
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
            Platform.run(runnable);
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
        final RC rc = new RC(false);
        // get the collector
        SCProfileInstance profileInstance = ScannerConfigProfileManager.getInstance().
                getSCProfileInstance(project, buildInfo.getSelectedProfileId());
        final IScannerInfoCollector collector = profileInstance.getScannerInfoCollector();
        final IExternalScannerInfoProvider esiProvider = profileInstance.
                createBuildOutputProvider();

        if (buildInfo.isBuildOutputFileActionEnabled()) {
            ISafeRunnable runnable = new ISafeRunnable() {
                
                public void run() {
                    esiProvider.invokeProvider(monitor, project, null, buildInfo, collector);
                    rc.set(true);
                }
        
                public void handleException(Throwable exception) {
                    rc.set(false);
                    MakeCorePlugin.log(exception);
                }
                
            };
            Platform.run(runnable);
        }
        
        return rc.get();
    }

}
