/*******************************************************************************
 * Copyright (c) 2004, 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.launching; 

import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.debug.service.IDsfDebugServicesFactory;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunchDelegate;
import org.eclipse.cdt.dsf.gdb.service.GdbDebugServicesFactory;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.ServiceFactoriesManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
 
/**
 * The launch configuration delegate for the DSF GDB JUnit tests.
 */
@ThreadSafe
public class TestLaunchDelegate extends GdbLaunchDelegate
{
	// A Test Debug Services factory that may be provided for the associated test case
	private GdbDebugServicesFactory fTestServicesFactory;

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
    public boolean preLaunchCheck(ILaunchConfiguration config, String mode, IProgressMonitor monitor) throws CoreException {
    	// Make sure we start the launch with no pre-defined services factory
    	fTestServicesFactory = null;
    	// Don't override the base method to allow it to set the GdbProcessFactory
    	// which LaunchConfigurationAndRestartTest.testExitCodeSet() depends on
    	return super.preLaunchCheck(config, mode, monitor);
    }
    
    @Override
	public boolean finalLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
    	// Check if this test has registered a services factory for this launch
    	String servicesFactoryId = configuration.getAttribute(ServiceFactoriesManager.DEBUG_SERVICES_FACTORY_KEY, "");
    	if (servicesFactoryId.length() > 0) {
    		// A services factory has been registered, so lets resolve it
    		fTestServicesFactory = BaseTestCase.getServiceFactoriesManager().removeTestServicesFactory(servicesFactoryId);
    	}
        return true;
    }
    
    @Override
    protected IPath checkBinaryDetails(ILaunchConfiguration config) throws CoreException {
    	// Now that GdbLaunchDelegate supports project-less debugging, we don't need to
    	// override this method.  In fact, we should not override it so that we test
    	// that project-less debugging keeps on working.
    	// See bug 343861
    	return super.checkBinaryDetails(config); 
    }

	@Override
	protected IDsfDebugServicesFactory newServiceFactory(ILaunchConfiguration config, String version) {
		if (fTestServicesFactory != null) {
			// A services factory has been provided for this test case
			return fTestServicesFactory;
		}

		// Use the original services factory
		return super.newServiceFactory(config, version);
	}
}