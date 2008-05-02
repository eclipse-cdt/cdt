/*******************************************************************************
 * Copyright (c) 2007 - 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Andy Jin - Hardware debugging UI improvements, bug 229946
 *******************************************************************************/

package org.eclipse.cdt.debug.gdbjtag.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.GDBJtagDeviceContribution;
import org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.GDBJtagDeviceContributionFactory;
import org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice;
import org.eclipse.cdt.debug.mi.core.AbstractGDBCDIDebugger;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MIPlugin;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.mi.core.command.CLICommand;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIGDBSetNewConsole;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * @author Doug Schaefer, Adrian Petrescu
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
		if (targets.length == 0 || !(targets[0] instanceof Target)) {
			Activator.log(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(),
					DebugPlugin.INTERNAL_ERROR, "Error getting debug target", null));
			return;
		}
		MISession miSession = ((Target)targets[0]).getMISession();
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
		
		IGDBJtagDevice gdbJtagDevice;
		try {
			gdbJtagDevice = getGDBJtagDevice(config);
		} catch (NullPointerException e) {
			return;
		}
		
		ArrayList commands = new ArrayList();
		
		// hook up to remote target
		boolean useRemote = config.getAttribute(IGDBJtagConstants.ATTR_USE_REMOTE_TARGET, IGDBJtagConstants.DEFAULT_USE_REMOTE_TARGET);
		if (useRemote) {
			monitor.beginTask("Connecting to remote", 1);
			String ipAddress = config.getAttribute(IGDBJtagConstants.ATTR_IP_ADDRESS, IGDBJtagConstants.DEFAULT_IP_ADDRESS);
			int portNumber = config.getAttribute(IGDBJtagConstants.ATTR_PORT_NUMBER, IGDBJtagConstants.DEFAULT_PORT_NUMBER);
			gdbJtagDevice.doRemote(ipAddress, portNumber, commands);
			executeGDBScript(getGDBScript(commands), miSession);
		}
		
		// execute init script
		monitor.beginTask("Executing init commands", 1);
		
		// Run device-specific code to reset the board
		if (config.getAttribute(IGDBJtagConstants.ATTR_DO_RESET, true)) {
			commands = new ArrayList();
			gdbJtagDevice.doReset(commands);
			int defaultDelay = gdbJtagDevice.getDefaultDelay();
			gdbJtagDevice.doDelay(config.getAttribute(IGDBJtagConstants.ATTR_DELAY, defaultDelay), commands);
			gdbJtagDevice.doHalt(commands);
			executeGDBScript(getGDBScript(commands), miSession);
		}
		// execute any user defined init command
		executeGDBScript(config, IGDBJtagConstants.ATTR_INIT_COMMANDS, miSession);

		// execute load
		boolean doLoad = config.getAttribute(IGDBJtagConstants.ATTR_LOAD_IMAGE, IGDBJtagConstants.DEFAULT_LOAD_IMAGE);
		if (doLoad) {
			// Escape windows path separator characters TWICE, once for Java and once for GDB.
			String imageFileName = config.getAttribute(IGDBJtagConstants.ATTR_IMAGE_FILE_NAME, "");
			if (imageFileName.length() > 0) {
				monitor.beginTask("Loading image", 1);
				imageFileName = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(imageFileName).replace("\\", "\\\\");
				String imageOffset = (imageFileName.endsWith(".elf")) ? "" : "0x" + config.getAttribute(IGDBJtagConstants.ATTR_IMAGE_OFFSET, "");
				commands = new ArrayList();
				gdbJtagDevice.doLoadImage(imageFileName, imageOffset, commands);
				executeGDBScript(getGDBScript(commands), miSession);

			}
		}
		
		// execute symbol load
		boolean doLoadSymbols = config.getAttribute(IGDBJtagConstants.ATTR_LOAD_SYMBOLS, IGDBJtagConstants.DEFAULT_LOAD_SYMBOLS);
		if (doLoadSymbols) {
			String symbolsFileName = config.getAttribute(IGDBJtagConstants.ATTR_SYMBOLS_FILE_NAME, "");
			if (symbolsFileName.length() > 0) {
				symbolsFileName = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(symbolsFileName).replace("\\", "\\\\");
				String symbolsOffset = "0x" + config.getAttribute(IGDBJtagConstants.ATTR_SYMBOLS_OFFSET, "");
				commands = new ArrayList();
				gdbJtagDevice.doLoadSymbol(symbolsFileName, symbolsOffset, commands);
				executeGDBScript(getGDBScript(commands), miSession);
			}
		}
	}

	public void doRunSession(ILaunch launch, ICDISession session, IProgressMonitor monitor) throws CoreException {
		ILaunchConfiguration config = launch.getLaunchConfiguration();
		ICDITarget[] targets = session.getTargets();
		if ( targets.length == 0 || !(targets[0] instanceof Target) )
			return;
		MISession miSession = ((Target)targets[0]).getMISession();
		
		IGDBJtagDevice gdbJtagDevice;
		try {
			gdbJtagDevice = getGDBJtagDevice(config);
		} catch (NullPointerException e) {
			return;
		}
		
		ArrayList commands = new ArrayList();
		// Set program counter
		String pcRegister = config.getAttribute(IGDBJtagConstants.ATTR_PC_REGISTER, config.getAttribute(IGDBJtagConstants.ATTR_IMAGE_OFFSET, ""));
		gdbJtagDevice.doSetPC(pcRegister, commands);
		executeGDBScript(getGDBScript(commands), miSession);
		
		// execute run script
		monitor.beginTask("Executing run commands", 1);
		boolean setStopAt = config.getAttribute(IGDBJtagConstants.ATTR_SET_STOP_AT, IGDBJtagConstants.DEFAULT_SET_STOP_AT);
		if (setStopAt) {
			String stopAt = config.getAttribute(IGDBJtagConstants.ATTR_STOP_AT, "");
			commands = new ArrayList();
			gdbJtagDevice.doStopAt(stopAt, commands);
			executeGDBScript(getGDBScript(commands), miSession);
		}
		
		boolean setResume = config.getAttribute(IGDBJtagConstants.ATTR_SET_RESUME, IGDBJtagConstants.DEFAULT_SET_RESUME);
		if (setResume) {
			commands = new ArrayList();
			gdbJtagDevice.doContinue(commands);
			executeGDBScript(getGDBScript(commands), miSession);
		}
		// Run any user defined command
		executeGDBScript(config, IGDBJtagConstants.ATTR_RUN_COMMANDS, miSession);
	}
	
	private void executeGDBScript(String script, MISession miSession) throws CoreException {
		// Try to execute any extra command
		if (script == null)
			return;
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
	
	private void executeGDBScript(ILaunchConfiguration configuration, String attribute,
			MISession miSession) throws CoreException {
		executeGDBScript(configuration.getAttribute(attribute, ""), miSession);
	}

	private IGDBJtagDevice getGDBJtagDevice (ILaunchConfiguration config) 
		throws CoreException, NullPointerException {
		IGDBJtagDevice gdbJtagDevice = null;
		String jtagDeviceName = config.getAttribute(IGDBJtagConstants.ATTR_JTAG_DEVICE, "");
		GDBJtagDeviceContribution[] availableDevices = GDBJtagDeviceContributionFactory.
			getInstance().getGDBJtagDeviceContribution();
		for (int i = 0; i < availableDevices.length; i++) {
			if (jtagDeviceName.equals(availableDevices[i].getDeviceName())) {
				gdbJtagDevice = availableDevices[i].getDevice();
				break;
			}
		}
		return gdbJtagDevice;
	}
	
	private String getGDBScript(Collection commands) {
		if (commands.isEmpty())
			return null;
		StringBuffer sb = new StringBuffer();
		Iterator it = commands.iterator();
		while (it.hasNext()) {
			sb.append(it.next());
		}
		return sb.toString();
	}
}
