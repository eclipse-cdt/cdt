/********************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [174945] Remove obsolete icons from rse.shells.ui
 ********************************************************************************/

package org.eclipse.rse.internal.shells.ui.actions;

import java.io.FileWriter;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.rse.internal.shells.ui.ShellResources;
import org.eclipse.rse.internal.shells.ui.ShellsUIPlugin;
import org.eclipse.rse.internal.shells.ui.view.SystemCommandsUI;
import org.eclipse.rse.internal.subsystems.files.core.SystemFileResources;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * @author dmcknigh
 */
public class SystemExportShellOutputAction extends SystemBaseShellAction
{
    public SystemExportShellOutputAction(Shell parent)
    {
        super(ShellResources.ACTION_EXPORT_SHELL_OUTPUT_LABEL,			
        		ShellResources.ACTION_EXPORT_SHELL_OUTPUT_TOOLTIP,
        		ShellsUIPlugin.getDefault().getImageDescriptor(ShellsUIPlugin.ICON_SYSTEM_EXPORT_SHELL_OUTPUT_ID),
			parent);
        allowOnMultipleSelection(false);
    }
    

	/**
	 * Called when this action is selected from the popup menu.
	 */
	public void run()
	{
		//SystemCommandsViewPart viewPart = \ 
		SystemCommandsUI.getInstance().activateCommandsView();
		for (int i = 0; i < _selected.size(); i++)
		{
		    IRemoteCommandShell cmdShell = (IRemoteCommandShell)_selected.get(i);

			FileDialog fdlg = new FileDialog(getShell(),SWT.SAVE);
			fdlg.setText(SystemFileResources.RESID_ENTER_OR_SELECT_FILE_TITLE);
			fdlg.setFileName(cmdShell.getId() + "-output.txt"); //$NON-NLS-1$
			fdlg.setFilterExtensions(new String[] {"*.txt"}); //$NON-NLS-1$
			String fileName = fdlg.open();
			try
			{
			    
			    //FileOutputStream stream = new FileOutputStream(fileName);
			    //OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8");
			    // dkm - using local encoding
			    FileWriter writer = new FileWriter(fileName);

			    Object[] outputs = cmdShell.listOutput();
			    for (int o = 0; o < outputs.length; o++)
			    {
			        IAdaptable output = (IAdaptable)outputs[o];
			        ISystemViewElementAdapter adapter = (ISystemViewElementAdapter)output.getAdapter(ISystemViewElementAdapter.class);
			        if (adapter != null)
			        {
			            writer.write(adapter.getText(output));
			            writer.write("\r\n"); //$NON-NLS-1$
			        }
			    }
			    writer.close();
			}
			catch (Exception e)
			{			    
			}
		}
	}
    
}