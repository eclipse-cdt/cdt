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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.rse.files.ui.actions.SystemAbstractRemoteFilePopupMenuExtensionAction;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.shells.ui.RemoteCommandHelpers;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;

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
			MessageDialog.openError(getShell(), e.getClass().getName(), e.getLocalizedMessage());
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
			//Option A: run the command invisibly
			//cmdss.runCommand(command, null, "", false); //$NON-NLS-1$
			//Option B: run the command in a visible shell
			RemoteCommandHelpers.runUniversalCommand(getShell(), command, ".", cmdss); //$NON-NLS-1$
		} else {
			MessageDialog.openError(getShell(), "No command subsystem", "Found no command subsystem");
		}
	}

}
