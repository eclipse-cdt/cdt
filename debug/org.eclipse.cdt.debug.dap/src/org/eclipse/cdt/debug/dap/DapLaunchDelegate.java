/*******************************************************************************
 * Copyright (c) 2019 Kichwa Coders and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.debug.dap;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.launch.AbstractCLaunchDelegate2;
import org.eclipse.cdt.launch.LaunchUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.sourcelookup.IPersistableSourceLocator2;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.lsp4e.debug.launcher.DSPLaunchDelegate;
import org.eclipse.lsp4e.debug.launcher.DSPLaunchDelegate.DSPLaunchDelegateLaunchBuilder;
import org.eclipse.lsp4e.debug.sourcelookup.DSPSourceLookupDirector;
import org.eclipse.swt.widgets.Display;

public class DapLaunchDelegate extends AbstractCLaunchDelegate2 {

	private static final String DEBUG_ADAPTER_JS = "/debug-servers/node_modules/cdt-gdb-adapter/dist/debugAdapter.js"; //$NON-NLS-1$
	// see https://github.com/eclipse-cdt/cdt-gdb-adapter/blob/73a31934d169555a338f53512b4994c017f67a1a/src/GDBDebugSession.ts#L22
	private static final String GDB = "gdb"; //$NON-NLS-1$
	private static final String PROGRAM = "program"; //$NON-NLS-1$
	private static final String ARGUMENTS = "arguments"; //$NON-NLS-1$
	private static final String VERBOSE = "verbose"; //$NON-NLS-1$
	private static final String LOG_FILE = "logFile"; //$NON-NLS-1$
	private InitializeLaunchConfigurations initializeLaunchConfigurations = new InitializeLaunchConfigurations(
			this::warnNodeJSMissing);

	private void warnNodeJSMissing() {
		Display.getDefault().asyncExec(() -> {
			MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "Missing node.js", //$NON-NLS-1$
					"Could not find node.js. This prevents being able to debug with the CDT Debug Adapter.\n" //$NON-NLS-1$
							+ "Please make sure node.js is installed and that your PATH environement variable contains the location to the `node` executable."); //$NON-NLS-1$
		});
	}

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		// user settings
		Map<String, Object> param = new HashMap<>();

		param.put(PROGRAM, LaunchUtils.getProgramPath(configuration));
		param.put(ARGUMENTS, LaunchUtils.getProgramArguments(configuration));

		try {
			URL fileURL = FileLocator.toFileURL(getClass().getResource(DEBUG_ADAPTER_JS));
			if (fileURL == null) {
				throw new IOException(Messages.DapLaunchDelegate_missing_debugAdapter_script + Activator.PLUGIN_ID
						+ DEBUG_ADAPTER_JS);
			}
			String path = fileURL.getPath();
			List<String> debugCmdArgs = Collections.singletonList(path);

			DSPLaunchDelegateLaunchBuilder builder = new DSPLaunchDelegateLaunchBuilder(configuration, mode, launch,
					monitor);
			builder.setLaunchDebugAdapter(initializeLaunchConfigurations.getNodeJsLocation()
					.orElseThrow(() -> new IOException("Cannot find node runtime")), debugCmdArgs); //$NON-NLS-1$
			builder.setMonitorDebugAdapter(true);
			builder.setDspParameters(param);

			new DSPLaunchDelegate().launch(builder);
		} catch (IOException e) {
			IStatus errorStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
			Activator.getDefault().getLog().log(errorStatus);
			ErrorDialog.openError(Display.getDefault().getActiveShell(), "Debug error", e.getMessage(), errorStatus); //$NON-NLS-1$
		}
	}

	@Override
	protected String getPluginID() {
		return Activator.PLUGIN_ID;
	}

	@Override
	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode) throws CoreException {
		IPersistableSourceLocator locator = createLocator(configuration);
		ILaunch launch = new Launch(configuration, mode, locator);
		return launch;
	}

	private IPersistableSourceLocator createLocator(ILaunchConfiguration configuration) throws CoreException {
		String type = DSPSourceLookupDirector.ID;
		IPersistableSourceLocator locator = DebugPlugin.getDefault().getLaunchManager().newSourceLocator(type);
		String memento = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, (String) null);
		if (memento == null) {
			locator.initializeDefaults(configuration);
		} else {
			if (locator instanceof IPersistableSourceLocator2) {
				((IPersistableSourceLocator2) locator).initializeFromMemento(memento, configuration);
			} else {
				locator.initializeFromMemento(memento);
			}
		}
		return locator;
	}

}
