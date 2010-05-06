/*******************************************************************************
 * Copyright (c) 2007 - 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer, Adrian Petrescu - QNX Software Systems - Initial API and implementation
 *     Andy Jin - Hardware debugging UI improvements, bug 229946
 *     Peter Vidler  - Monitor support (progress and cancellation) bug 242699
 *     Bruce Griffith, Sage Electronic Engineering, LLC - bug 305943
 *              - API generalization to become transport-independent (allow
 *                connections via serial ports and pipes).
 *******************************************************************************/

package org.eclipse.cdt.debug.gdbjtag.core;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Debugger class for Jtag debugging (using gdb server)
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
	
	@SuppressWarnings("deprecation")
	protected void doStartSession(ILaunch launch, Session session, IProgressMonitor monitor) throws CoreException {
		SubMonitor submonitor = SubMonitor.convert(monitor, 100);
		
		try {
			submonitor.subTask(Messages.getString("GDBJtagDebugger.0")); //$NON-NLS-1$
			ILaunchConfiguration config = launch.getLaunchConfiguration();
			ICDITarget[] targets = session.getTargets();
			if (targets.length == 0 || !(targets[0] instanceof Target)) {
				Activator.log(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(),
						DebugPlugin.INTERNAL_ERROR, Messages.getString("GDBJtagDebugger.1"), null)); //$NON-NLS-1$
				return;
			}
			MISession miSession = ((Target)targets[0]).getMISession();
			CommandFactory factory = miSession.getCommandFactory();
			if (submonitor.isCanceled()) {
				throw new OperationCanceledException();
			}
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

			submonitor.worked(10);
			if (submonitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			IGDBJtagDevice gdbJtagDevice;
			try {
				gdbJtagDevice = getGDBJtagDevice(config);
			} catch (NullPointerException e) {
				return;
			}

			List<String> commands = new ArrayList<String>();

			// hook up to remote target
			if (submonitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			boolean useRemote = config.getAttribute(IGDBJtagConstants.ATTR_USE_REMOTE_TARGET, IGDBJtagConstants.DEFAULT_USE_REMOTE_TARGET);
			if (useRemote) {
				submonitor.subTask(Messages.getString("GDBJtagDebugger.2")); //$NON-NLS-1$
				try {
					if (gdbJtagDevice instanceof IGDBJtagConnection) { 
						URI	connection = new URI(config.getAttribute(IGDBJtagConstants.ATTR_CONNECTION, IGDBJtagConstants.DEFAULT_CONNECTION));
						IGDBJtagConnection device = (IGDBJtagConnection)gdbJtagDevice;
						device.doRemote(connection.getSchemeSpecificPart(), commands);
					} else {
						// use deprecated methods tied to TCP/IP
						String ipAddress = config.getAttribute(IGDBJtagConstants.ATTR_IP_ADDRESS, "");  //$NON-NLS-1$
						int portNumber = config.getAttribute(IGDBJtagConstants.ATTR_PORT_NUMBER, 0);
						gdbJtagDevice.doRemote(ipAddress, portNumber, commands);
					}
				} catch (URISyntaxException e) {
					throw new OperationCanceledException();
				}
				executeGDBScript(getGDBScript(commands), miSession, submonitor.newChild(10));
				if (submonitor.isCanceled()) {
					throw new OperationCanceledException();
				}
			}

			// execute init script
			submonitor.subTask(Messages.getString("GDBJtagDebugger.3")); //$NON-NLS-1$
			submonitor.setWorkRemaining(80); // compensate for optional work above

			// Run device-specific code to reset the board
			if (config.getAttribute(IGDBJtagConstants.ATTR_DO_RESET, true)) {
				commands.clear();
				gdbJtagDevice.doReset(commands);
				int defaultDelay = gdbJtagDevice.getDefaultDelay();
				gdbJtagDevice.doDelay(config.getAttribute(IGDBJtagConstants.ATTR_DELAY, defaultDelay), commands);
				executeGDBScript(getGDBScript(commands), miSession, submonitor.newChild(15));
			}
			submonitor.setWorkRemaining(65); // compensate for optional work above

			// Run device-specific code to halt the board
			if (config.getAttribute(IGDBJtagConstants.ATTR_DO_HALT, true)) {
				commands.clear();
				gdbJtagDevice.doHalt(commands);
				executeGDBScript(getGDBScript(commands), miSession, submonitor.newChild(15));
			}
			submonitor.setWorkRemaining(50); // compensate for optional work above
			// execute any user defined init command
			executeGDBScript(config, IGDBJtagConstants.ATTR_INIT_COMMANDS, miSession,
					submonitor.newChild(15));

			// execute load
			boolean doLoad = config.getAttribute(IGDBJtagConstants.ATTR_LOAD_IMAGE, IGDBJtagConstants.DEFAULT_LOAD_IMAGE);
			if (doLoad) {
				// Escape windows path separator characters TWICE, once for Java and once for GDB.
				String imageFileName = config.getAttribute(IGDBJtagConstants.ATTR_IMAGE_FILE_NAME, ""); //$NON-NLS-1$
				if (imageFileName.length() > 0) {
					monitor.beginTask(Messages.getString("GDBJtagDebugger.5"), 1); //$NON-NLS-1$
					imageFileName = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(imageFileName).replace("\\", "\\\\");
					String imageOffset = (imageFileName.endsWith(".elf")) ? "" : "0x" + config.getAttribute(IGDBJtagConstants.ATTR_IMAGE_OFFSET, ""); //$NON-NLS-2$ //$NON-NLS-4$
					commands.clear();
					gdbJtagDevice.doLoadImage(imageFileName, imageOffset, commands);
					executeGDBScript(getGDBScript(commands), miSession, submonitor.newChild(20));
				}
			}
			submonitor.setWorkRemaining(15); // compensate for optional work above

			// execute symbol load
			boolean doLoadSymbols = config.getAttribute(IGDBJtagConstants.ATTR_LOAD_SYMBOLS, IGDBJtagConstants.DEFAULT_LOAD_SYMBOLS);
			if (doLoadSymbols) {
				String symbolsFileName = config.getAttribute(IGDBJtagConstants.ATTR_SYMBOLS_FILE_NAME, ""); //$NON-NLS-1$
				if (symbolsFileName.length() > 0) {
					symbolsFileName = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(symbolsFileName).replace("\\", "\\\\");
					String symbolsOffset = "0x" + config.getAttribute(IGDBJtagConstants.ATTR_SYMBOLS_OFFSET, ""); //$NON-NLS-2$
					commands.clear();
					gdbJtagDevice.doLoadSymbol(symbolsFileName, symbolsOffset, commands);
					executeGDBScript(getGDBScript(commands), miSession, submonitor.newChild(15));
				}
			}
		} catch (OperationCanceledException e) {
			if (launch != null && launch.canTerminate()) {
				launch.terminate();
			}
		}
	}

	public void doRunSession(ILaunch launch, ICDISession session, IProgressMonitor monitor) throws CoreException {
		SubMonitor submonitor = SubMonitor.convert(monitor, 100);
		
		try {
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

			if (submonitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			submonitor.worked(20);
			List<String> commands = new ArrayList<String>();
			// Set program counter
			boolean setPc = config.getAttribute(IGDBJtagConstants.ATTR_SET_PC_REGISTER, IGDBJtagConstants.DEFAULT_SET_PC_REGISTER);
			if (setPc) {
				String pcRegister = config.getAttribute(IGDBJtagConstants.ATTR_PC_REGISTER, config.getAttribute(IGDBJtagConstants.ATTR_IMAGE_OFFSET, "")); //$NON-NLS-1$
				gdbJtagDevice.doSetPC(pcRegister, commands);
				executeGDBScript(getGDBScript(commands), miSession, submonitor.newChild(20));
			}
			submonitor.setWorkRemaining(60); // compensate for optional work above

			// execute run script
			monitor.beginTask(Messages.getString("GDBJtagDebugger.18"), 1); //$NON-NLS-1$
			boolean setStopAt = config.getAttribute(IGDBJtagConstants.ATTR_SET_STOP_AT, IGDBJtagConstants.DEFAULT_SET_STOP_AT);
			if (setStopAt) {
				String stopAt = config.getAttribute(IGDBJtagConstants.ATTR_STOP_AT, ""); //$NON-NLS-1$
				commands.clear();
				gdbJtagDevice.doStopAt(stopAt, commands);
				executeGDBScript(getGDBScript(commands), miSession, submonitor.newChild(20));
			}
			submonitor.setWorkRemaining(40); // compensate for optional work above

			boolean setResume = config.getAttribute(IGDBJtagConstants.ATTR_SET_RESUME, IGDBJtagConstants.DEFAULT_SET_RESUME);
			if (setResume) {
				commands.clear();
				gdbJtagDevice.doContinue(commands);
				executeGDBScript(getGDBScript(commands), miSession, submonitor.newChild(20));
			}
			submonitor.setWorkRemaining(20); // compensate for optional work above
			// Run any user defined command
			executeGDBScript(config, IGDBJtagConstants.ATTR_RUN_COMMANDS, miSession, 
					submonitor.newChild(20));
		} catch (OperationCanceledException e) {
			if (launch != null && launch.canTerminate()) {
				launch.terminate();
			}
		}
	}
	
	private void executeGDBScript(String script, MISession miSession, 
			IProgressMonitor monitor) throws CoreException {
		// Try to execute any extra command
		if (script == null)
			return;
		script = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(script);
		String[] commands = script.split("\\r?\\n");
		SubMonitor submonitor = SubMonitor.convert(monitor, commands.length);
		for (int j = 0; j < commands.length; ++j) {
			try {
				submonitor.subTask(Messages.getString("GDBJtagDebugger.21") + commands[j]); //$NON-NLS-1$
				CLICommand cli = new CLICommand(commands[j]);
				miSession.postCommand(cli, MISession.FOREVER);
				submonitor.worked(1);
				if (submonitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				MIInfo info = cli.getMIInfo();
				if (info == null) {
					throw new MIException("Timeout"); //$NON-NLS-1$
				}
			} catch (MIException e) {
				MultiStatus status = new MultiStatus(
						Activator.PLUGIN_ID,
						ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR,
						Messages.getString("GDBJtagDebugger.22"), e); //$NON-NLS-1$
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
			MISession miSession, IProgressMonitor monitor) throws CoreException {
		executeGDBScript(configuration.getAttribute(attribute, ""), miSession, monitor); //$NON-NLS-1$
	}

	private IGDBJtagDevice getGDBJtagDevice (ILaunchConfiguration config) 
		throws CoreException, NullPointerException {
		IGDBJtagDevice gdbJtagDevice = null;
		String jtagDeviceName = config.getAttribute(IGDBJtagConstants.ATTR_JTAG_DEVICE, ""); //$NON-NLS-1$
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
	
	private String getGDBScript(List<String> commands) {
		if (commands.isEmpty())
			return null;
		StringBuffer sb = new StringBuffer();
		for (String cmd : commands) {
			sb.append(cmd);
		}
		return sb.toString();
	}
}
