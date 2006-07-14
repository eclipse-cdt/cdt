/********************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation. All rights reserved.
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
import java.util.Iterator;

import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystemConfiguration;
import org.eclipse.rse.ui.actions.SystemAbstractPopupMenuExtensionAction;
import org.eclipse.ui.IObjectActionDelegate;


/**
 * This is a base class to simplify the creation of actions supplied via the
 * org.eclipse.rse.ui.popupMenus extension point, targeting remote files
 * and/or remote folders.
 * <p>
 * The only method you must implement is {@link #run()}.
 * You may optionally override {@link #getEnabled(Object[])}
 * <p>
 * Convenience methods available in this class:
 * <ul>
 *   <li>{@link #getSelectedRemoteFiles()}
 *   <li>{@link #getFirstSelectedRemoteFile()}
 *   <li>{@link #getRemoteFileSubSystem()}
 *   <li>{@link #getRemoteFileSubSystemFactory()}
 * </ul>
 * <p>
 * See also the convenience methods available in the parent class {@link SystemAbstractPopupMenuExtensionAction}
 * 
 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter
 * @see org.eclipse.rse.ui.dialogs.SystemPromptDialog
 */
public abstract class SystemAbstractRemoteFilePopupMenuExtensionAction 
       extends    SystemAbstractPopupMenuExtensionAction
       implements IObjectActionDelegate 
{

	/**
	 * Constructor 
	 */
	public SystemAbstractRemoteFilePopupMenuExtensionAction() 
	{
		super();
	}

	// ----------------------------------
	// OVERRIDABLE METHODS FROM PARENT...
	// ----------------------------------
	
	/**
	 * The user has selected this action. This is where the actual code for the action goes.
	 */
	public abstract void run();

	/**
	 * The user has selected one or more objects. This is an opportunity to enable/disable 
	 *  this action based on the current selection. 
	 * <p>
	 * The default implementation of this method returns false if all the objects are not of
	 * type IRemoteFile.
	 */
	public boolean getEnabled(Object[] currentlySelected)
	{
		for (int idx=0; idx<currentlySelected.length; idx++)
		   if (!(currentlySelected[idx] instanceof IRemoteFile))
		     return false;
		return true;
	}

	// ------------------------------------------------------------------
	// CONVENIENCE METHODS WE ADD SPECIFICALLY FOR REMOTE FILE ACTIONS...
	// ------------------------------------------------------------------

	/**
	 * Retrieve the currently selected objects as an array of IRemoteFile objects.
	 * Array may be length 0, but will never be null, for convenience.
	 */
	public IRemoteFile[] getSelectedRemoteFiles()
	{
		IRemoteFile[] seld = new IRemoteFile[(sel!=null) ? sel.size() : 0];
		if (sel == null)
		  return seld;
		Iterator i = sel.iterator();
		int idx=0;
		while (i.hasNext())
		  seld[idx++] = (IRemoteFile)i.next();
		return seld;
	}
	/**
	 * Retrieve the first selected object, as an IRemoteFile, for convenience.
	 * Will be null if there is nothing selected
	 */
	public IRemoteFile getFirstSelectedRemoteFile()
	{
		//System.out.println("Sel = " + sel);
		if (sel == null)
		  return null;
		Object obj = sel.getFirstElement();
		//System.out.println("obj = " + obj);
		if (obj instanceof IRemoteFile)
		  return (IRemoteFile)obj;
		else
		  return null;
	}

    /**
     * Get the remote file subsystem from which the selected objects were resolved.
     * This has many useful methods in it, including support to transfer files to and
     * from the local and remote systems.
     */
    public IRemoteFileSubSystem getRemoteFileSubSystem()
    {
    	return (IRemoteFileSubSystem)getSubSystem();
    }
    
    /**
     * Returns the remote file subsystem factory which owns the subsystem from which the 
     * selected remote objects were resolved. This has some useful methods in it, 
     * including isUnixStyle() indicating if this remote file system is unix or windows.
     */
    public IRemoteFileSubSystemConfiguration getRemoteFileSubSystemFactory()
    {
    	return (IRemoteFileSubSystemConfiguration)getSubSystemFactory();
    }    
    
}