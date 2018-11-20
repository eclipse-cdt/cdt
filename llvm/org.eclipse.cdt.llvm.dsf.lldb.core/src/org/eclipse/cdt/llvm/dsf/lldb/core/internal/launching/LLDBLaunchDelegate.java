/*******************************************************************************
 * Copyright (c) 2016 Ericsson.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.llvm.dsf.lldb.core.internal.launching;

import org.eclipse.cdt.dsf.debug.service.IDsfDebugServicesFactory;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunchDelegate;
import org.eclipse.cdt.llvm.dsf.lldb.core.internal.service.LLDBServiceFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ISourceLocator;

/**
 * A specific LLDB launch delegate that allows customization such as setting the
 * LLDB path, using LLDB-specific preferences and creating a service factory
 * (mostly used to work around the current LLDB-MI limitations)
 */
public class LLDBLaunchDelegate extends GdbLaunchDelegate {

	/**
	 * Constructs the {@link LLDBLaunchDelegate}.
	 *
	 * This is meant to be called by the plug-in registry (plugin.xml)
	 */
	public LLDBLaunchDelegate() {
		super();
	}

	/**
	 * Constructs the {@link LLDBLaunchDelegate}.
	 *
	 * @param requireCProject whether or not debugging requires a C/C++ project. For example, in attach mode is is not required.
	 */
	public LLDBLaunchDelegate(boolean requireCProject) {
		super(requireCProject);
	}

	/*
	 * TODO: The fact that both getCLILabel and GdbLaunch.getGDBPath have to be
	 * overridden and made consistent seems error prone. getCLILabel should call
	 * GdbLaunch.getGDBPath somehow by default. This is something that should be
	 * looked into in dsf-gdb.
	 */
	@Override
	protected String getCLILabel(ILaunchConfiguration config, String gdbVersion) throws CoreException {
		return LLDBLaunch.getLLDBPath(config).toString().trim() + " (" + Messages.LLDBLaunchDelegate_mimicking_gdb //$NON-NLS-1$
				+ " gdb " + gdbVersion + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	protected IDsfDebugServicesFactory newServiceFactory(ILaunchConfiguration config, String version) {
		return new LLDBServiceFactory(version, config);
	}

	@Override
	protected GdbLaunch createGdbLaunch(ILaunchConfiguration configuration, String mode, ISourceLocator locator)
			throws CoreException {
		return new LLDBLaunch(configuration, mode, locator);
	}
}
