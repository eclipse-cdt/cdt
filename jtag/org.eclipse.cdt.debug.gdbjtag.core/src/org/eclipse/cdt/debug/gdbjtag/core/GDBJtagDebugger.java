/**********************************************************************
 * Copyright (c) 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     QNX Software Systems - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.debug.gdbjtag.core;

import java.io.File;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.mi.core.AbstractGDBCDIDebugger;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MIPlugin;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.mi.core.command.CLICommand;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIGDBSetNewConsole;
import org.eclipse.cdt.debug.mi.core.command.MITargetDownload;
import org.eclipse.cdt.debug.mi.core.command.MITargetSelect;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * @author Doug Schaefer
 *
 */
public class GDBJtagDebugger extends AbstractGDBCDIDebugger {

	public ICDISession createSession(ILaunch launch, File executable,
			IProgressMonitor monitor) throws CoreException {
		return super.createSession(launch, executable, monitor);
	}

	public ICDISession createDebuggerSession(ILaunch launch, IBinaryObject exe,
			IProgressMonitor monitor) throws CoreException {
		return super.createDebuggerSession(launch, exe, monitor);
	}
	
	protected CommandFactory getCommandFactory(ILaunchConfiguration config)
			throws CoreException {
		String miVersion = MIPlugin.getMIVersion(config);
		return new GDBJtagCommandFactory(miVersion);
	}
	
	protected void doStartSession(ILaunch launch, Session session, IProgressMonitor monitor) throws CoreException {
		ILaunchConfiguration config = launch.getLaunchConfiguration();
		ICDITarget[] targets = session.getTargets();
		if (targets.length == 0 || !(targets[0] instanceof Target))
			return ; // TODO should raise an exception
		MISession miSession = ((Target)targets[0]).getMISession();
		getMISession(session);
		CommandFactory factory = miSession.getCommandFactory();
		try {
			MIGDBSetNewConsole newConsole = factory.createMIGDBSetNewConsole();
			miSession.postCommand(newConsole);
			MIInfo info = newConsole.getMIInfo();
			if (info == null) {
				throw new MIException(MIPlugin.getResourceString("src.common.No_answer")); //$NON-NLS-1$
			}
		}
		catch( MIException e ) {
			// We ignore this exception, for example
			// on GNU/Linux the new-console is an error.
		}
		
		// hook up to remote target
		boolean useRemote = config.getAttribute(IGDBJtagConstants.ATTR_USE_REMOTE_TARGET, IGDBJtagConstants.DEFAULT_USE_REMOTE_TARGET);
		if (useRemote) {
			try {
				monitor.beginTask("Connecting to remote", 1);
				String ipAddress = config.getAttribute(IGDBJtagConstants.ATTR_IP_ADDRESS, IGDBJtagConstants.DEFAULT_IP_ADDRESS);
				int portNumber = config.getAttribute(IGDBJtagConstants.ATTR_PORT_NUMBER, IGDBJtagConstants.DEFAULT_PORT_NUMBER);
				String address = ipAddress + ":" + String.valueOf(portNumber);
				MITargetSelect targetSelect = factory.createMITargetSelect(new String[] { "remote", address });
				miSession.postCommand(targetSelect);
				MIInfo info = targetSelect.getMIInfo();
				if (info == null) {
					throw new MIException(MIPlugin.getResourceString("src.common.No_answer")); //$NON-NLS-1$
				}
			} catch (MIException e) {
				// TODO dunno...
			}
		}
		
		// execute init script
		monitor.beginTask("Executing init commands", 1);
		executeGDBScript(config, IGDBJtagConstants.ATTR_INIT_COMMANDS, miSession);

		// execute load
		boolean doLoad = config.getAttribute(IGDBJtagConstants.ATTR_LOAD_IMAGE,	IGDBJtagConstants.DEFAULT_LOAD_IMAGE);
		if (doLoad) {
			String imageFileName = config.getAttribute(IGDBJtagConstants.ATTR_IMAGE_FILE_NAME, "");
			if (imageFileName.length() > 0) {
				try {
					monitor.beginTask("Loading image", 1);
					imageFileName = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(imageFileName);
					MITargetDownload download = factory.createMITargetDownload(imageFileName);
					miSession.postCommand(download, MISession.FOREVER);
					MIInfo info = download.getMIInfo();
					if (info == null) {
						throw new MIException(MIPlugin.getResourceString("src.common.No_answer")); //$NON-NLS-1$
					}
				} catch (MIException e) {
					// TODO dunno...
				}
			}
		}
	}
	
	protected MISession getMISession(Session session) {
		ICDITarget[] targets = session.getTargets();
		if (targets.length == 0 || !(targets[0] instanceof Target))
			return null;
		return ((Target)targets[0]).getMISession();
	}

	public void doRunSession(ILaunch launch, ICDISession session, IProgressMonitor monitor) throws CoreException {
		ILaunchConfiguration config = launch.getLaunchConfiguration();
		ICDITarget[] targets = session.getTargets();
		if ( targets.length == 0 || !(targets[0] instanceof Target) )
			return;
		MISession miSession = ((Target)targets[0]).getMISession();

		// execute run script
		monitor.beginTask("Executing run commands", 1);
		executeGDBScript(config, IGDBJtagConstants.ATTR_RUN_COMMANDS, miSession);
	}
	
	private void executeGDBScript(ILaunchConfiguration configuration, String attribute,
			MISession miSession) throws CoreException {
		// Try to execute any extrac comand
		String script = configuration.getAttribute(attribute, ""); //$NON-NLS-1$
		script = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(script);
		String[] commands = script.split("\\r?\\n");
		for (int j = 0; j < commands.length; ++j) {
			try {
				CLICommand cli = new CLICommand(commands[j]);
				miSession.postCommand(cli, MISession.FOREVER);
				MIInfo info = cli.getMIInfo();
				if (info == null) {
					throw new MIException("Timeout"); //$NON-NLS-1$
				}
			} catch (MIException e) {
				MultiStatus status = new MultiStatus(
						Activator.PLUGIN_ID,
						ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR,
						"Failed command", e);
				status
						.add(new Status(
								IStatus.ERROR,
								Activator.PLUGIN_ID,
								ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR,
								e == null ? "" : e.getLocalizedMessage(), //$NON-NLS-1$
								e));
				CDebugCorePlugin.log(status);
			}
		}
	}

}
