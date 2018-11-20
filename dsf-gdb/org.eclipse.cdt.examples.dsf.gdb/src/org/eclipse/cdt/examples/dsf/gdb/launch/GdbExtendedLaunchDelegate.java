/*******************************************************************************
 * Copyright (c) 2014, 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Marc Khouzam (Ericsson)   - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.gdb.launch;

import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.debug.service.IDsfDebugServicesFactory;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunchDelegate;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.examples.dsf.gdb.GDBExamplePlugin;
import org.eclipse.cdt.examples.dsf.gdb.service.GdbExtendedDebugServicesFactory;
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
	protected GdbLaunch createGdbLaunch(ILaunchConfiguration configuration, String mode, ISourceLocator locator)
			throws CoreException {
		return new GdbExtendedLaunch(configuration, mode, locator);
	}

	@Override
	protected Sequence getServicesSequence(DsfSession session, ILaunch launch, IProgressMonitor rm) {
		return new GdbExtendedServicesLaunchSequence(session, (GdbLaunch) launch, rm);
	}

	@Override
	protected IDsfDebugServicesFactory newServiceFactory(ILaunchConfiguration config, String version) {
		return new GdbExtendedDebugServicesFactory(version, config);
	}

	@Override
	protected String getPluginID() {
		return GDBExamplePlugin.PLUGIN_ID;
	}
}
