/*******************************************************************************
 * Copyright (c) 2004, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.launching; 

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunchDelegate;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
 
/**
 * The launch configuration delegate for the DSF GDB JUnit tests.
 */
@ThreadSafe
public class TestLaunchDelegate extends GdbLaunchDelegate
{
	@Override
	protected IProject[] getBuildOrder(ILaunchConfiguration configuration,
			String mode) throws CoreException {
		return null;
	}
	@Override
	protected IProject[] getProjectsForProblemSearch(
			ILaunchConfiguration configuration, String mode)
			throws CoreException {
		return null;
	}
	
    @Override
	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
        return false;
    }

    @Override
	public boolean finalLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
        return true;
    }
    
    @Override
    protected IPath checkBinaryDetails(ILaunchConfiguration config) throws CoreException {
    	return new Path(config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, "")); 
    }

}