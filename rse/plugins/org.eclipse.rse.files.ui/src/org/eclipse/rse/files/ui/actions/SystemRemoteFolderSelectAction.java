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
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.files.ui.actions;

import org.eclipse.rse.files.ui.dialogs.SystemRemoteFolderDialog;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.ui.dialogs.SystemRemoteResourceDialog;
import org.eclipse.swt.widgets.Shell;


public class SystemRemoteFolderSelectAction extends
		SystemRemoteFileSelectAction
{
	public SystemRemoteFolderSelectAction(Shell shell)
	{
		super(shell);
	}
	
	public SystemRemoteFolderSelectAction(Shell shell, String label, String tooltip)
	{
		super(shell, label, tooltip);
	}
	
    protected SystemRemoteResourceDialog createRemoteResourceDialog(Shell shell, String title)
    {
    	return new SystemRemoteFolderDialog(shell, title);
    }
    
    protected SystemRemoteResourceDialog createRemoteResourceDialog(Shell shell)
    {
    	return new SystemRemoteFolderDialog(shell);
    }
    
    /**
     * Retrieve selected folder object. If multiple folders selected, returns the first.
     */
    public IRemoteFile getSelectedFolder()
    {
    	Object o = getValue();
    	if (o instanceof IRemoteFile[])
    	  return ((IRemoteFile[])o)[0];
    	else if (o instanceof IRemoteFile)
    	  return (IRemoteFile)o;
        else
    	  return null;
    }
    /**
     * Retrieve selected folder objects. If no folders selected, returns an array of zero.
     * If one folder selected returns an array of one.
     */
    public IRemoteFile[] getSelectedFolders()
    {
    	Object o = getValue();
    	if (o instanceof Object[]) {
    		
    		Object[] temp = (Object[])o;
    			
    		IRemoteFile[] files = new IRemoteFile[temp.length];
    		
    		// ensure all objects are IRemoteFiles
    		for (int i = 0; i < temp.length; i++) {
    			
    			if (temp[i] instanceof IRemoteFile) {
    				files[i] = (IRemoteFile)temp[i];
    			}
    			// should never happen
    			else {
    				return new IRemoteFile[0];
    			}
    		}
    			
    		return files;
    	}
    	if (o instanceof IRemoteFile[])
    	  return (IRemoteFile[])o;
    	else if (o instanceof IRemoteFile)
    	  return new IRemoteFile[] {(IRemoteFile)o};
        else
    	  return new IRemoteFile[0];
    }
}