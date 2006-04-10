/********************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.shells.ui.actions;

import java.io.FileWriter;

import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.shells.ui.ShellResources;
import org.eclipse.rse.shells.ui.view.SystemCommandsUI;
import org.eclipse.rse.shells.ui.view.SystemCommandsViewPart;
import org.eclipse.rse.subsystems.files.core.SystemFileResources;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;



/**
 * @author dmcknigh
 */
public class SystemExportShellHistoryAction extends SystemBaseShellAction
{
    public SystemExportShellHistoryAction(Shell parent)
    {
        super(ShellResources.ACTION_EXPORT_SHELL_HISTORY_LABEL,			
        		ShellResources.ACTION_EXPORT_SHELL_HISTORY_TOOLTIP,
			SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_EXPORT_SHELL_HISTORY_ID),
			parent); 
        allowOnMultipleSelection(false);
    }
    

	/**
	 * Called when this action is selected from the popup menu.
	 */
	public void run()
	{
		SystemCommandsViewPart viewPart = SystemCommandsUI.getInstance().activateCommandsView();
		for (int i = 0; i < _selected.size(); i++)
		{
		    IRemoteCommandShell cmdShell = (IRemoteCommandShell)_selected.get(i);

		    
			FileDialog fdlg = new FileDialog(getShell(), SWT.SAVE);
			fdlg.setText(SystemFileResources.RESID_ENTER_OR_SELECT_FILE_TITLE);
			//fdlg.setText(SystemPlugin.getString("com.ibm.etools.systems.ui.RmtJarExport.selectOrEnterJarDialog.title"));
			fdlg.setFileName(cmdShell.getId() + "-history.txt");
			fdlg.setFilterExtensions(new String[] {"*.txt"});
			String fileName = fdlg.open();
			try
			{
			    //FileOutputStream stream = new FileOutputStream(fileName);
			    //OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8");
			    // DKM - file writer writes to local encoding
			    FileWriter writer= new FileWriter(fileName);
			    String[] cmds = cmdShell.getHistory();
			    for (int o = 0; o < cmds.length; o++)
			    {
			        String cmd = cmds[o];
			        writer.write(cmd);
			        writer.write("\r\n");
			       
			    }
			    writer.close();
			}
			catch (Exception e)
			{			    
			}
		}
	}
    
}