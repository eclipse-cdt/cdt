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

package org.eclipse.rse.ui.actions;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.rse.model.IHost;
import org.eclipse.swt.widgets.Shell;


/**
 * This is the base class for actions that populate the "Remote Servers" cascading
 *  menu.  The actions in this menu cascade again, into Start and Stop actions. 
 *  These cascading actions are handling automatically by this base class, but when
 *  they are run, they call back into abstract methods in this base class, to 
 *  actually start and stop the remote server/daemon.
 * 
 */
public abstract class SystemCascadingRemoteServerBaseAction extends SystemBaseSubMenuAction implements  IMenuListener
{
	private SystemRemoteServerStartAction startAction;
	private SystemRemoteServerStopAction  stopAction;

	/**
	 * Constructor 
	 * @deprecated
	 */
	public SystemCascadingRemoteServerBaseAction(String label, String tooltip, Shell shell)
	{
		super(label, tooltip, shell);
        setCreateMenuEachTime(false);
        setPopulateMenuEachTime(false);
	}

	/**
	 * Populate the submenu for this action. Here is where we add the start and stop actions.
	 */
	public IMenuManager populateSubMenu(IMenuManager menu)
	{
		startAction = new SystemRemoteServerStartAction(getShell(), this);
		stopAction = new SystemRemoteServerStopAction(getShell(), this);
		startAction.setHelp(getHelpContextId());
		stopAction.setHelp(getHelpContextId());
		menu.add(startAction);
		menu.add(stopAction);		
		menu.addMenuListener(this); // we want to know when menu is about to be shown.
		//System.out.println("in populateSubMenu in "+getClass().getName());
		return menu;
	}

	/**
	 * Called when submenu is about to show.
	 * We use this to decide whether to enable/disable the start and stop actions underneath.
	 */
	public void menuAboutToShow(IMenuManager subMenu)
	{
		//System.out.println("menuAboutToShow");
		/*
		IStructuredSelection selection = getSelection();
		if( selection == null )
		{
			subMenu.add(new SystemBaseAction("Programming error. Selection is null! ", null));
			return;
		} // end if(nothing is selected)
		*/
		if (!isEnabled())
		{
			startAction.setEnabled(false);
			stopAction.setEnabled(false);
		}
		else if (canDetectServerState())
		{
			//System.out.println("in action itself. getSystemConnection() = "+getSystemConnection());
			boolean started = isServerStarted(getSystemConnection());
			//System.out.println("... started? "+started);
			startAction.setEnabled(started);
			stopAction.setEnabled(!started);
		}		
	}
	/**
	 * This method is an opportunity to decide whether to enable this action or not. 
	 * Sometimes we can determine if the connection is connected or not, in which case
	 * we want to disable this action if the connection is not connected.
	 */
	protected boolean shouldEnable(IHost connection)
	{
		return true;
	}

	/**
	 * Overridable method to tell the base code if you are able to determine dynamically if 
	 * the server is currently running or not. The default is true, and so {@link #isStarted(IHost)} is 
	 * called to determine enablement state of the cascading start and stop actions. If you
	 * cannot determine this, override this method and return false, and both start and stop
	 * will be enabled always. 
	 * @return true if you have the capability of dynamically determining if this server is running.
	 */
	protected boolean canDetectServerState()
	{
		return true;
	}
	
	/**
	 * Overridable method to compute if this remote server/daemon is currently running or not.
	 * This decides the enablement of the start and stop actions underneath. If there is no way
	 * to determine this, return false from {@link #canDetectServerState()}, and ignore this method.  
	 * @return true if the server is currently running or not.
	 */
	protected abstract boolean isServerStarted(IHost connection);
	
	/**
	 * Callback from the {@link org.eclipse.rse.ui.actions.SystemRemoteServerStartAction} class
	 *  that is called when the user selects to start this remote server/daemon.
	 * @return true if the remote server was successfully started, false if not.
	 */
	public abstract boolean startServer();

	/**
	 * Callback from the {@link org.eclipse.rse.ui.actions.SystemRemoteServerStartAction} class
	 *  that is called when the user selects to stop this remote server/daemon.
	 * @return true if the remote server was successfully stopped, false if not.
	 */
	public abstract boolean stopServer();
	
}