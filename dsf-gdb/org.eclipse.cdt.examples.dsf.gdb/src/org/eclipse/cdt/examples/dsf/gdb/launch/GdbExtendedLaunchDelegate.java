/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Marc Khouzam (Ericsson)   - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.gdb.launch; 

import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.debug.service.IDsfDebugServicesFactory;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunchDelegate;
import org.eclipse.cdt.dsf.gdb.launching.LaunchUtils;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.examples.dsf.gdb.GDBExamplePlugin;
import org.eclipse.cdt.examples.dsf.gdb.service.GdbExtendedDebugServicesFactory;
import org.eclipse.cdt.examples.dsf.gdb.service.GdbExtendedDebugServicesFactoryNS;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ISourceLocator;
 
public class GdbExtendedLaunchDelegate extends GdbLaunchDelegate {
	public GdbExtendedLaunchDelegate() {
		super();
	}

    @Override
	protected GdbLaunch createGdbLaunch(ILaunchConfiguration configuration, String mode, ISourceLocator locator) throws CoreException {
    	return new GdbExtendedLaunch(configuration, mode, locator);
    }

    @Override
    protected Sequence getServicesSequence(DsfSession session, ILaunch launch, IProgressMonitor rm) {
   		return new GdbExtendedServicesLaunchSequence(session, (GdbLaunch)launch, rm);
    }

    @Override
	protected IDsfDebugServicesFactory newServiceFactory(ILaunchConfiguration config, String version) {
		boolean nonStop = LaunchUtils.getIsNonStopMode(config);
		if (nonStop && isNonStopSupportedInGdbVersion(version)) {
			return new GdbExtendedDebugServicesFactoryNS(version);
		}
		return new GdbExtendedDebugServicesFactory(version);
	}

	@Override
	protected String getPluginID() {
		return GDBExamplePlugin.PLUGIN_ID;
	}
}
