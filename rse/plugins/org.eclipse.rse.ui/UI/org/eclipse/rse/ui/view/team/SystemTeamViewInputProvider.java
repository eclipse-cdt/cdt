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

package org.eclipse.rse.ui.view.team;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.SystemResourceManager;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.ui.view.ISystemViewInputProvider;
import org.eclipse.swt.widgets.Shell;


/**
 * Represents the input to the team viewer.
 * For now, this really doesn't do much since we always list the same thing.
 */
public class SystemTeamViewInputProvider implements IAdaptable, ISystemViewInputProvider
{
	private Object[] roots = new Object[1]; 
	private Shell shell;
	private Viewer viewer;
			
	/**
	 * Constructor for SystemTeamViewInputProvider.
	 */
	public SystemTeamViewInputProvider() 
	{
		super();
	}
	
	/**
	 * Return the roots to display in the team viewer.
	 * This is simply the RSE singleton project
	 */
	public Object[] getRoots()
	{
		if (roots[0] == null)
          roots[0] = SystemResourceManager.getRemoteSystemsProject();
        return roots;
	}
	
    /**
	 * This is the method required by the IAdaptable interface.
	 * Given an adapter class type, return an object castable to the type, or
	 *  null if this is not possible.
	 */
    public Object getAdapter(Class adapterType)
    {
   	    return Platform.getAdapterManager().getAdapter(this, adapterType);	
    }

	// ----------------------------------------    
    // Methods from ISystemViewInputProvider...
    // ----------------------------------------

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemViewInputProvider#getSystemViewRoots()
	 */
	public Object[] getSystemViewRoots()
	{
		return getRoots();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemViewInputProvider#hasSystemViewRoots()
	 */
	public boolean hasSystemViewRoots()
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemViewInputProvider#showingConnections()
	 */
	public boolean showingConnections()
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemViewInputProvider#getConnectionChildren(org.eclipse.rse.model.IHost)
	 */
	public Object[] getConnectionChildren(IHost selectedConnection)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemViewInputProvider#hasConnectionChildren(org.eclipse.rse.model.IHost)
	 */
	public boolean hasConnectionChildren(IHost selectedConnection)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemViewInputProvider#setShell(org.eclipse.swt.widgets.Shell)
	 */
	public void setShell(Shell shell)
	{
		this.shell = shell;		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemViewInputProvider#getShell()
	 */
	public Shell getShell()
	{
		return shell;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemViewInputProvider#setViewer(org.eclipse.jface.viewers.Viewer)
	 */
	public void setViewer(Viewer viewer)
	{
		this.viewer = viewer;		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemViewInputProvider#getViewer()
	 */
	public Viewer getViewer()
	{
		return viewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemViewInputProvider#showActionBar()
	 */
	public boolean showActionBar()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemViewInputProvider#showButtonBar()
	 */
	public boolean showButtonBar()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemViewInputProvider#showActions()
	 */
	public boolean showActions()
	{
		return false;
	}           
	
}