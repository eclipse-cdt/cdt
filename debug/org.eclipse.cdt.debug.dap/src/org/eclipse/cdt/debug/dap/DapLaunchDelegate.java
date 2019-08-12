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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
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
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.sourcelookup.IPersistableSourceLocator2;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.lsp4e.debug.launcher.DSPLaunchDelegate;
import org.eclipse.lsp4e.debug.launcher.DSPLaunchDelegate.DSPLaunchDelegateLaunchBuilder;
import org.eclipse.lsp4e.debug.sourcelookup.DSPSourceLookupDirector;
import org.eclipse.swt.widgets.Display;

public class DapLaunchDelegate extends AbstractCLaunchDelegate2 {

	private static final String DEBUG_ADAPTER_ROOT = "/debug-servers/node_modules/cdt-gdb-adapter/dist/"; //$NON-NLS-1$
	private static final String DEBUG_ADAPTER_JS = "debugAdapter.js"; //$NON-NLS-1$
	private static final String DEBUG_TARGET_ADAPTER_JS = "debugTargetAdapter.js"; //$NON-NLS-1$
	// https://github.com/eclipse-cdt/cdt-gdb-adapter/blob/eccf6bbb091aedd855adf0eaa1b28d341d8405d5/src/GDBDebugSession.ts#L22
	public static final String GDB = "gdb"; //$NON-NLS-1$
	public static final String PROGRAM = "program"; //$NON-NLS-1$
	public static final String ARGUMENTS = "arguments"; //$NON-NLS-1$
	public static final String VERBOSE = "verbose"; //$NON-NLS-1$
	public static final String LOG_FILE = "logFile"; //$NON-NLS-1$
	public static final String INIT_COMMANDS = "initCommands"; //$NON-NLS-1$

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
		Map<String, Object> params = new HashMap<>();

		// To support transitioning from DSF to DAP we support the IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME
		// which is part of DSF but really is a generic setting
		params.put(GDB, org.eclipse.cdt.dsf.gdb.launching.LaunchUtils.getGDBPath(configuration).toOSString());
		params.put(PROGRAM, LaunchUtils.getProgramPath(configuration));
		params.put(ARGUMENTS, LaunchUtils.getProgramArguments(configuration));
		// Explicitly default to launch -- this is the default encoded in DSPDebugTarget.initialize()
		params.put("request", "launch"); //$NON-NLS-1$//$NON-NLS-2$
		launch(configuration, mode, launch, monitor, false, params);
	}

	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor,
			boolean targetDebugAdapter, Map<String, Object> params) throws CoreException {
		String debugAdapterJs = DEBUG_ADAPTER_ROOT + (targetDebugAdapter ? DEBUG_TARGET_ADAPTER_JS : DEBUG_ADAPTER_JS);
		try {
			URL fileURL = FileLocator.toFileURL(Activator.getDefault().getClass().getResource(debugAdapterJs));
			if (fileURL == null) {
				throw new IOException(
						Messages.DapLaunchDelegate_missing_debugAdapter_script + Activator.PLUGIN_ID + debugAdapterJs);
			}
			String path = new File(fileURL.toURI()).toString();
			List<String> debugCmdArgs = Collections.singletonList(path);

			DSPLaunchDelegateLaunchBuilder builder = new DSPLaunchDelegateLaunchBuilder(configuration, mode, launch,
					monitor);
			builder.setLaunchDebugAdapter(initializeLaunchConfigurations.getNodeJsLocation()
					.orElseThrow(() -> new IOException("Cannot find node runtime")), debugCmdArgs); //$NON-NLS-1$
			builder.setMonitorDebugAdapter(true);
			builder.setDspParameters(params);

			DSPLaunchDelegate dspLaunchDelegate = new DSPLaunchDelegate() {
				@Override
				protected IDebugTarget createDebugTarget(SubMonitor subMonitor, Runnable cleanup,
						InputStream inputStream, java.io.OutputStream outputStream, ILaunch launch,
						Map<String, Object> dspParameters) throws CoreException {
					DapDebugTarget target = new DapDebugTarget(launch, cleanup, inputStream, outputStream,
							dspParameters);
					target.initialize(subMonitor.split(80));
					return target;
				}
			};
			dspLaunchDelegate.launch(builder);
		} catch (IOException | URISyntaxException e) {
			IStatus errorStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
			Activator.getDefault().getLog().log(errorStatus);
			Display.getDefault().asyncExec(() -> {
				ErrorDialog.openError(Display.getDefault().getActiveShell(), "Debug error", e.getMessage(), //$NON-NLS-1$
						errorStatus);
			});
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
