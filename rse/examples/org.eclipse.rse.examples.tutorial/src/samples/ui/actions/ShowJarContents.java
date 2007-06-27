/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - Adapted original tutorial code to Open RSE.
 * David Dykstal (IBM) - formatting for tutorial
 ********************************************************************************/

package samples.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.shells.IHostOutput;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IHostShellChangeEvent;
import org.eclipse.rse.services.shells.IHostShellOutputListener;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.shells.ui.RemoteCommandHelpers;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteError;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteOutput;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.IShellServiceSubSystem;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * An action that runs a command to display the contents of a Jar file.
 * The plugin.xml file restricts this action so it only appears for .jar files.
 */
public class ShowJarContents implements IObjectActionDelegate {
	private List _selectedFiles;

	/**
	 * Constructor for ShowJarContents.
	 */
	public ShowJarContents() {
		_selectedFiles = new ArrayList();
	}

	protected Shell getShell() {
		return SystemBasePlugin.getActiveWorkbenchShell();
	}

	protected IRemoteFile getFirstSelectedRemoteFile() {
		if (_selectedFiles.size() > 0) {
			return (IRemoteFile) _selectedFiles.get(0);
		}
		return null;
	}

	protected ISubSystem getSubSystem() {
		return getFirstSelectedRemoteFile().getParentRemoteFileSubSystem();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		IRemoteFile selectedFile = getFirstSelectedRemoteFile();
		String cmdToRun = "jar -tvf " + selectedFile.getAbsolutePath(); //$NON-NLS-1$
		try {
			runCommand(cmdToRun);
		} catch (Exception e) {
			String excType = e.getClass().getName();
			MessageDialog.openError(getShell(), excType, excType + ": " + e.getLocalizedMessage()); //$NON-NLS-1$
			e.printStackTrace();
		}
	}

	public IRemoteCmdSubSystem getRemoteCmdSubSystem() {
		//get the Command subsystem associated with the current host
		IHost myHost = getSubSystem().getHost();
		IRemoteCmdSubSystem[] subsys = RemoteCommandHelpers.getCmdSubSystems(myHost);
		for (int i = 0; i < subsys.length; i++) {
			if (subsys[i].getSubSystemConfiguration().supportsCommands()) {
				return subsys[i];
			}
		}
		return null;
	}

	public void runCommand(String command) throws Exception {
		IRemoteCmdSubSystem cmdss = getRemoteCmdSubSystem();
		if (cmdss != null && cmdss.isConnected()) {
			//Option A: run the command invisibly through SubSystem API
			//runCommandInvisibly(cmdss, command);
			//Option B: run the command invisibly through Service API
			//runCommandInvisiblyService(cmdss, command);
			//Option C: run the command in a visible shell
			RemoteCommandHelpers.runUniversalCommand(getShell(), command, ".", cmdss); //$NON-NLS-1$
		} else {
			MessageDialog.openError(getShell(), "No command subsystem", "Found no command subsystem");
		}
	}

	public static class StdOutOutputListener implements IHostShellOutputListener {
		public void shellOutputChanged(IHostShellChangeEvent event) {
			IHostOutput[] lines = event.getLines();
			for (int i = 0; i < lines.length; i++) {
				System.out.println(lines[i]);
			}
		}
	}

	/** New version of running commands through IShellService / IHostShell */
	public void runCommandInvisiblyService(IRemoteCmdSubSystem cmdss, String command) throws Exception {
		if (cmdss instanceof IShellServiceSubSystem) {
			IShellService shellService = ((IShellServiceSubSystem) cmdss).getShellService();
			String[] environment = new String[1];
			environment[0] = "AAA=BBB"; //$NON-NLS-1$
			String initialWorkingDirectory = "."; //$NON-NLS-1$

			IHostShell hostShell = shellService.launchShell(initialWorkingDirectory, environment, new NullProgressMonitor());
			hostShell.addOutputListener(new StdOutOutputListener());
			//hostShell.writeToShell("pwd"); //$NON-NLS-1$
			//hostShell.writeToShell("echo ${AAA}"); //$NON-NLS-1$
			//hostShell.writeToShell("env"); //$NON-NLS-1$
			hostShell.writeToShell(command);
			hostShell.writeToShell("exit"); //$NON-NLS-1$
		}
	}

	/** Old version of running commands through the command subsystem */
	public void runCommandInvisibly(IRemoteCmdSubSystem cmdss, String command) throws Exception {
		command = command + cmdss.getParentRemoteCmdSubSystemConfiguration().getCommandSeparator() + "exit"; //$NON-NLS-1$
		Object[] result = cmdss.runCommand(command, null, false, new NullProgressMonitor());
		if (result.length > 0 && result[0] instanceof IRemoteCommandShell) {
			IRemoteCommandShell cs = (IRemoteCommandShell) result[0];
			while (cs.isActive()) {
				Thread.sleep(1000);
			}
			Object[] output = cs.listOutput();
			for (int i = 0; i < output.length; i++) {
				if (output[i] instanceof IRemoteOutput) {
					System.out.println(((IRemoteOutput) output[i]).getText());
				} else if (output[i] instanceof IRemoteError) {
					System.err.println(((IRemoteError) output[i]).getText());
				}
			}
			cmdss.removeShell(cs);
		}
	}

	public void selectionChanged(org.eclipse.jface.action.IAction action, org.eclipse.jface.viewers.ISelection selection) {
		_selectedFiles.clear();
		// store the selected jars to be used when running
		Iterator theSet = ((IStructuredSelection) selection).iterator();
		while (theSet.hasNext()) {
			Object obj = theSet.next();
			if (obj instanceof IRemoteFile) {
				_selectedFiles.add(obj);
			}
		}
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}
}
