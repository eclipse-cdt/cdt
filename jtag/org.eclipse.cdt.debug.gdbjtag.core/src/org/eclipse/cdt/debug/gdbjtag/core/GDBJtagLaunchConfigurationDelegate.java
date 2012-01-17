/*******************************************************************************
 * Copyright (c) 2007 - 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.gdbjtag.core;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;

/**
 * @author Doug Schaefer
 * 
 */
public class GDBJtagLaunchConfigurationDelegate extends AbstractCLaunchDelegate {

	@Override
	protected String getPluginID() {
		return Activator.PLUGIN_ID;
	};

	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		SubMonitor submonitor = SubMonitor.convert(monitor, 2);
		// set the default source locator if required
		setDefaultSourceLocator(launch, configuration);

		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			GDBJtagDebugger debugger = new GDBJtagDebugger();
			ICProject project = CDebugUtils.verifyCProject(configuration);
			IPath exePath = CDebugUtils.verifyProgramPath(configuration);
			ICDISession session = debugger.createSession(launch, null, submonitor.newChild(1));
			IBinaryObject exeBinary = null;
			if ( exePath != null ) {
				exeBinary = verifyBinary(project, exePath);
			}
			
			try {
				// create the Launch targets/processes for eclipse.
				ICDITarget[] targets = session.getTargets();
				for( int i = 0; i < targets.length; i++ ) {
					Process process = targets[i].getProcess();
					IProcess iprocess = null;
					if ( process != null ) {
						iprocess = DebugPlugin.newProcess(launch, process, renderProcessLabel(exePath != null ? exePath.toOSString() : "???"),
								getDefaultProcessMap() );
					}
					CDIDebugModel.newDebugTarget(launch, project.getProject(), targets[i],
							renderProcessLabel("GDB Hardware Debugger"), iprocess, exeBinary, true, false, false);
				}
				
				debugger.doRunSession(launch, session, submonitor.newChild(1));
			} catch (CoreException e) {
				try {
					session.terminate();
				} catch (CDIException e1) {
					// ignore
				}
				throw e;
			}
		} else {
			cancel("TargetConfiguration not supported",
					ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		}
	}

}
