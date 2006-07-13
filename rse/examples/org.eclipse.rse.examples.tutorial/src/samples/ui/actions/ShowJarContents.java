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

import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.files.ui.actions.SystemAbstractRemoteFilePopupMenuExtensionAction;
import org.eclipse.rse.internal.model.SystemRegistry;
import org.eclipse.rse.model.IHost;
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
			//TODO: Display exception
		}
	}

	public IRemoteCmdSubSystem getRemoteCmdSubSystem() {
		//get the Command subsystem associated with the current host
		IHost myHost = getSubSystem().getHost();
		ISubSystem[] subsys = SystemRegistry.getSystemRegistry().getSubSystems(myHost);
		for (int i=0; i<subsys.length; i++) {
			if (subsys[i] instanceof IRemoteCmdSubSystem) {
				IRemoteCmdSubSystem ss = (IRemoteCmdSubSystem) subsys[i];
				if (ss.getSubSystemConfiguration().supportsCommands()) {
					// && ss.isConnected()
					// TODO: Check, if the remote cmd subsys is capable of connecting on demand
					return ss;
				}
			}
		}
		return null;
	}
	
	public void runCommand(String command) throws Exception
	{
		IRemoteCmdSubSystem cmdss = getRemoteCmdSubSystem();
		if (cmdss!=null && cmdss.isConnected()) {
			Object[] result = cmdss.runCommand(command, null, "", false); //$NON-NLS-1$
			//TODO: Display the result, or is this done in the Shell automatically?
		} else {
			//TODO: Display error - no command subsystem available
		}
	}

}
