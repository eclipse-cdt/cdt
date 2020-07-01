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
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.osgi.util.NLS;

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

	@Override
	protected String getCLILabel(GdbLaunch launch, ILaunchConfiguration config, String gdbVersion)
			throws CoreException {
		IPath path = launch.getGDBPath();
		if (path == null) {
			path = LLDBLaunch.getLLDBPath(config);
		}
		return NLS.bind(Messages.LLDBLaunchDelegate_cli_label, path.toString().trim(), gdbVersion);
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
