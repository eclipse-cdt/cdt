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

import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Build output reader job
 * 
 * @author vhirsl
 */
public class BuildOutputReaderJob extends Job {
	private static final String JOB_NAME = "Build Output Reader"; //$NON-NLS-1$
	
	private String inputFileName;

    private IResource resource;
    private IScannerConfigBuilderInfo2 buildInfo;
    private boolean rc;

	/**
     * @param project
     * @param buildInfo
     */
    public BuildOutputReaderJob(IProject project, IScannerConfigBuilderInfo2 buildInfo) {
        super(JOB_NAME);
        this.resource = project;
        this.buildInfo = buildInfo;
        setUser(true);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.core.internal.jobs.InternalJob#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IStatus run(IProgressMonitor monitor) {
        IProject project = resource.getProject();
        monitor.beginTask(MakeMessages.getString("ScannerConfigBuilder.Invoking_Builder"), 100); //$NON-NLS-1$
        monitor.subTask(MakeMessages.getString("ScannerConfigBuilder.Invoking_Builder") +   //$NON-NLS-1$ 
                project.getName());

        boolean rc = SCJobsUtil.readBuildOutputFile(project, buildInfo, new SubProgressMonitor(monitor, 70));
        rc |= SCJobsUtil.getProviderScannerInfo(project, buildInfo, new SubProgressMonitor(monitor, 20));
        if (rc) {
            rc = SCJobsUtil.updateScannerConfiguration(project, buildInfo, new SubProgressMonitor(monitor, 10));
        }
        
        monitor.done();
        return (rc == true) ? Status.OK_STATUS : Status.CANCEL_STATUS;
	}

}
