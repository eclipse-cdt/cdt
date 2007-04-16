/**********************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     QNX Software Systems - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.debug.gdbjtag.core;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;

/**
 * @author Doug Schaefer
 * 
 */
public class GDBJtagLaunchConfigurationDelegate extends AbstractCLaunchDelegate {

	protected String getPluginID() {
		return Activator.PLUGIN_ID;
	};

	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		try {
			// set the default source locator if required
			setDefaultSourceLocator(launch, configuration);

			if (mode.equals(ILaunchManager.DEBUG_MODE)) {
				ICDebugConfiguration debugConfig = getDebugConfig(configuration);
				ICDISession dsession = null;
				String debugMode = configuration
						.getAttribute(
								ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
								ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN);

				if (debugMode
						.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN)) {
//					dsession = ((EmbeddedGDBCDIDebugger) debugConfig
//							.createDebugger()).createDebuggerSession(this,
//							launch, exeFileInfo, new SubProgressMonitor(
//									monitor, 8));
//
//					ICDITarget[] dtargets = dsession.getTargets();
//					// setFactory(dtargets);
//					try {
//
//						monitor.worked(1);
//
//						executeGDBScript(
//								configuration,
//								LaunchConfigurationConstants.ATTR_DEBUGGER_COMMANDS_INIT,
//								dtargets);
//						monitor.worked(2);
//
//						queryTargetState(dtargets);
//
//						// create the Launch targets/processes for eclipse.
//						for (int i = 0; i < dtargets.length; i++) {
//							Target target = (Target) dtargets[i];
//							target.setConfiguration(new Configuration(target));
//							Process process = target.getProcess();
//							IProcess iprocess = null;
//							if (process != null) {
//								iprocess = DebugPlugin.newProcess(launch,
//										process, renderProcessLabel(exePath
//												.toOSString()));
//							}
//							CDIDebugModel.newDebugTarget(launch, projectInfo,
//									dtargets[i],
//									renderTargetLabel(debugConfig), iprocess,
//									exeFileInfo, true, true, false);
//							/* FIX!!!! put up a console view for */
//							// if (process != null) {
//							// iprocess = DebugPlugin.newProcess(launch,
//							// process,
//							// renderProcessLabel(exePath.toOSString()));
//							// }
//						}
//						executeGDBScript(
//								configuration,
//								LaunchConfigurationConstants.ATTR_DEBUGGER_COMMANDS_RUN,
//								dtargets);
//
//					} catch (CoreException e) {
//						try {
//							dsession.terminate();
//						} catch (CDIException e1) {
//							// ignore
//						}
//						throw e;
//					}
				}
			} else {
				cancel("TargetConfiguration not supported",
						ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
			}
		} finally {
			monitor.done();
		}

	}

}
