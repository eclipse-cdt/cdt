/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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
 ********************************************************************************/

package samples.ui.actions;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.files.ui.actions.SystemAbstractRemoteFilePopupMenuExtensionAction;
import org.eclipse.rse.internal.subsystems.shells.subsystems.RemoteError;
import org.eclipse.rse.internal.subsystems.shells.subsystems.RemoteOutput;
import org.eclipse.rse.services.shells.IHostOutput;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IHostShellChangeEvent;
import org.eclipse.rse.services.shells.IHostShellOutputListener;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.shells.ui.RemoteCommandHelpers;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.IShellServiceSubSystem;

/**
 * An action that runs a command to display the contents of a Jar file.
 * The plugin.xml file restricts this action so it only appears for .jar files.
 */
public class ShowJarContents extends SystemAbstractRemoteFilePopupMenuExtensionAction {

	/**
	 * Constructor for ShowJarContents.
	 */
	public ShowJarContents() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.actions.SystemAbstractPopupMenuExtensionAction#run()
	 */
	public void run() {
		IRemoteFile selectedFile = getFirstSelectedRemoteFile();
		String cmdToRun = "jar -tvf " + selectedFile.getAbsolutePath(); //$NON-NLS-1$
		try {
			runCommand(cmdToRun);
		} catch(Exception e) {
			String excType = e.getClass().getName();
			MessageDialog.openError(getShell(), excType, excType+": "+e.getLocalizedMessage()); //$NON-NLS-1$
			e.printStackTrace();
		}
	}

	public IRemoteCmdSubSystem getRemoteCmdSubSystem() {
		//get the Command subsystem associated with the current host
		IHost myHost = getSubSystem().getHost();
		IRemoteCmdSubSystem[] subsys = RemoteCommandHelpers.getCmdSubSystems(myHost);
		for (int i=0; i<subsys.length; i++) {
			if (subsys[i].getSubSystemConfiguration().supportsCommands()) {
				return subsys[i];
			}
		}
		return null;
	}
	
	public void runCommand(String command) throws Exception
	{
		IRemoteCmdSubSystem cmdss = getRemoteCmdSubSystem();
		if (cmdss!=null && cmdss.isConnected()) {
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
			for(int i=0; i<lines.length; i++) {
				System.out.println(lines[i]);
			}
		}
	}
	
	/** New version of running commands through IShellService / IHostShell */
	public void runCommandInvisiblyService(IRemoteCmdSubSystem cmdss, String command) throws Exception
	{
		if (cmdss instanceof IShellServiceSubSystem) {
			IShellService shellService = ((IShellServiceSubSystem)cmdss).getShellService();
	        String [] environment = new String[1];
	        environment[0] = "AAA=BBB"; //$NON-NLS-1$
	        String initialWorkingDirectory = "."; //$NON-NLS-1$

	        IHostShell hostShell = shellService.launchShell(new NullProgressMonitor(), initialWorkingDirectory, environment);
			hostShell.addOutputListener(new StdOutOutputListener());
	        //hostShell.writeToShell("pwd"); //$NON-NLS-1$
	        //hostShell.writeToShell("echo ${AAA}"); //$NON-NLS-1$
	        //hostShell.writeToShell("env"); //$NON-NLS-1$
	        hostShell.writeToShell(command);
	        hostShell.writeToShell("exit"); //$NON-NLS-1$
		}
	}
	
	/** Old version of running commands through the command subsystem */
	public void runCommandInvisibly(IRemoteCmdSubSystem cmdss, String command) throws Exception
	{
		command = command + cmdss.getParentRemoteCmdSubSystemConfiguration().getCommandSeparator() + "exit"; //$NON-NLS-1$
		Object[] result = cmdss.runCommand(command, null, false);
		if (result.length>0 && result[0] instanceof IRemoteCommandShell) {
			IRemoteCommandShell cs = (IRemoteCommandShell)result[0];
			while (cs.isActive()) {
				Thread.sleep(1000);
			}
			Object[] output = cs.listOutput();
			for (int i=0; i<output.length; i++) {
				if (output[i] instanceof RemoteOutput) {
					System.out.println(((RemoteOutput)output[i]).getText());
				} else if (output[i] instanceof RemoteError) {
					System.err.println(((RemoteError)output[i]).getText());
				}
			}
			cmdss.removeShell(cs);
		}
	}

}
