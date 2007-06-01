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
 * Martin Oberhuber (Wind River) - Add Javadoc
 ********************************************************************************/

package org.eclipse.rse.internal.files.ui.actions;

import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.files.ui.ISystemAddFileListener;
import org.eclipse.rse.files.ui.dialogs.SystemRemoteFolderDialog;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.ui.dialogs.SystemRemoteResourceDialog;
import org.eclipse.rse.ui.validators.IValidatorRemoteSelection;
import org.eclipse.swt.widgets.Shell;


/**
 * The action for allowing the user to select a remote folder.
 * <p>
 * To configure the functionality, call these methods:
 * <ul>
 *   <li>{@link #setShowNewConnectionPrompt(boolean)}
 *   <li>{@link #setHost(IHost) or #setDefaultConnection(SystemConnection)}
 *   <li>{@link #setSystemType(IRSESystemType)} or {@link #setSystemTypes(IRSESystemType[])}
 *   <li>{@link #setRootFolder(IHost, String)} or {@link #setRootFolder(IRemoteFile)} or {@link #setPreSelection(IRemoteFile)}
 *   <li>{@link #setPreSelection(IRemoteFile)}
 *   <li>{@link #setShowPropertySheet(boolean)}
 *   <li>{@link #enableAddMode(ISystemAddFileListener)}
 *   <li>{@link #setMultipleSelectionMode(boolean)}
 *   <li>{@link #setSelectionValidator(IValidatorRemoteSelection)}
 * </ul>
 * <p>
 * To configure the text on the dialog, call these methods:
 * <ul>
 *   <li>{@link #setDialogTitle(String)}
 *   <li>{@link #setMessage(String)}
 *   <li>{@link #setSelectionTreeToolTipText(String)}
 * </ul>
 * <p>
 * After running, call these methods to get the output:
 * <ul>
 *   <li>{@link #getSelectedFolder()} or {@link #getSelectedFolders()}
 *   <li>{@link #getSelectedConnection()}
 * </ul>
 */
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