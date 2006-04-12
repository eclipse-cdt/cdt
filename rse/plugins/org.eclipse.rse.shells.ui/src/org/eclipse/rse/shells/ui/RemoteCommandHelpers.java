/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.shells.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.subsystems.shells.subsystems.RemoteCmdSubSystem;
import org.eclipse.rse.internal.subsystems.shells.subsystems.RemoteCommandShell;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.shells.ui.view.SystemCommandsUI;
import org.eclipse.rse.shells.ui.view.SystemCommandsViewPart;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystemConfiguration;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.swt.widgets.Shell;


/**
 * Static helpers to simplify the act of running a universal command against
 *   a local or remote Unix, Windows or Linux command shell.
 */
public class RemoteCommandHelpers 
{
     
	 /**
	  * Constructor for RemoteCommandHelpers.
	  */
	 public RemoteCommandHelpers() 
	 {
		super();
	 }
	
	 /**
	  * Helper method to return the path to change-directory to, given a selected remote file object
	  */
	 public static String getWorkingDirectory(IRemoteFile selectedFile)
	 {
          String path = null;
          if (selectedFile.isDirectory())
            path = selectedFile.getAbsolutePath();
          else
            path = selectedFile.getParentPath();	 	  
          return path;
	 }


	public static IRemoteCmdSubSystem getCmdSubSystem(IHost connection)
	{
		ISystemRegistry sr = RSEUIPlugin.getTheSystemRegistry();
		ISubSystem[] sses = sr.getSubSystems(connection);
		for (int i = 0; i < sses.length; i++)
		{
			if (sses[i] instanceof IRemoteCmdSubSystem)	
			{
				IRemoteCmdSubSystem cmdSubSystem = (RemoteCmdSubSystem)sses[i];
				return cmdSubSystem;
			}
		}
		return null;
	}
	
	public static IRemoteCmdSubSystem[] getCmdSubSystems(IHost connection)
	{
		List results = new ArrayList();
		ISystemRegistry sr = RSEUIPlugin.getTheSystemRegistry();
		ISubSystem[] sses = sr.getSubSystems(connection);
		for (int i = 0; i < sses.length; i++)
		{
			if (sses[i] instanceof IRemoteCmdSubSystem)	
			{
				IRemoteCmdSubSystem cmdSubSystem = (RemoteCmdSubSystem)sses[i];
				results.add(cmdSubSystem);
			}
		}
		return (IRemoteCmdSubSystem[])results.toArray(new IRemoteCmdSubSystem[results.size()]);
	}
		
	public static boolean runUniversalCommand(Shell shell,  String cmdString, String path, 
												  IRemoteCmdSubSystem cmdSubSystem)
	{
		return runUniversalCommand(shell, cmdString, path, cmdSubSystem, false);
	}
	
     /**
      * Encapsulation of code needed to run a universal subsystem command. A universal 
      * command is a command that runs in a Unix or Linux or Windows command shell, as
      * opposed to something unique like an OS/400 or z/OS command. 
      * This:
      * <ul>
      *  <li>Sets the current directory to the given path
      *  <li>Runs the given command remotely
      *  <li>Logs the command and its output in the command view
      * </ul>
      * 
      * @param shell - the shell to use if need to prompt for password or show msg dialog
      * @param cmdString - the resolved command
      * @param path - the path to run the command against
      * @param cmdSubSystem - this connection's command subsystem, which will run the command
      * @return true if we should continue, false if something went wrong
      */
     public static boolean runUniversalCommand(Shell shell,  String cmdString, String path, 
                                                 IRemoteCmdSubSystem cmdSubSystem, boolean isCompile)
     {
     	  boolean ok = false;
          if (cmdSubSystem != null)
          {
          	  ok = true;
              try
              {              	 
              	
              	 IRemoteCommandShell defaultShell= cmdSubSystem.getDefaultShell(shell);  
          		
              	 
					showInView(defaultShell, isCompile, cmdString);   
					           	 
					 IRemoteFileSubSystemConfiguration fileSSF = RemoteFileUtility.getFileSubSystemFactory(cmdSubSystem.getHost().getSystemType());
					 IRemoteFile pwd = ((RemoteCommandShell)defaultShell).getWorkingDirectory();
	                if (pwd == null || !pwd.getAbsolutePath().equals(path))
	                {
	                    if (path.indexOf(' ') > 0)
	                    {
	                        path = "\"" + path + "\"";
	                    }
	                    
						 String cdCmd = "cd " + path;
	                	 if (!fileSSF.isUnixStyle())
	                	 {                	 
	                	 	if (path.endsWith(":"))
	                	 	{
	                	 		path += "\\";
	                	 	}
	                   		 cdCmd = "cd /d " + path;                  		                    		 
	                	 }	 
	                            
						cmdSubSystem.sendCommandToShell(cdCmd, shell, defaultShell);				   
	                }
                	cmdSubSystem.sendCommandToShell(cmdString, shell, defaultShell);
              	 
              
              }
              catch (Exception e)
              {
                SystemBasePlugin.logError("Run Remote Command failed", e);
                SystemMessageDialog.displayExceptionMessage(shell, e);
                ok = false;
              } 
          } // end if
          return ok;
     } // end method	 
     
     


	public static void showInView(IRemoteCommandShell cmd, boolean isCompile, String cmdString) {
        SystemCommandsViewPart cmdsPart = SystemCommandsUI.getInstance().activateCommandsView();
        cmdsPart.updateOutput(cmd);
        /* DKM - no longer show this for compile commands 
        if (isCompile) {
        	SystemBuildErrorViewPart errorPart = SystemCommandsUI.getInstance().activateBuildErrorView();
        	errorPart.setInput((IAdaptable)cmd, cmdString);
        }
        */
     }
}