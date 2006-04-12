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
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemProfile;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.dialogs.SystemRenameSingleDialog;
import org.eclipse.rse.ui.dialogs.SystemSimpleContentElement;
import org.eclipse.swt.widgets.Shell;


/**
 * Copy a connection action.
 */
public class SystemCopyConnectionAction extends SystemBaseCopyAction
       implements  ISystemMessages
{
	private ISystemRegistry sr = null;
	private SystemSimpleContentElement initialSelectionElement = null;
	/**
	 * Constructor for SystemCopyConnectionAction
	 */
	public SystemCopyConnectionAction(Shell parent) 
	{
		super(parent, SystemResources.ACTION_COPY_CONNECTION_LABEL, MODE_COPY);
		sr = RSEUIPlugin.getTheSystemRegistry();
  	    setHelp(RSEUIPlugin.HELPPREFIX+"actn0019"); 
  	    setDialogHelp(RSEUIPlugin.HELPPREFIX+"dccn0000"); 
	}
 
	/**
	 * We override from parent to do unique checking...
	 * <p>
	 * We intercept to ensure only connections from the same profile are selected.
	 * <p>
	 * @see SystemBaseAction#updateSelection(IStructuredSelection)
	 */
	public boolean updateSelection(IStructuredSelection selection)
	{
		boolean enable = true;
		ISystemRegistry sr = RSEUIPlugin.getDefault().getSystemRegistry();		
		ISystemProfile prevProfile = null;
		Iterator e= ((IStructuredSelection) selection).iterator();		
		while (enable && e.hasNext())
		{
			Object selectedObject = e.next();
			if (selectedObject instanceof IHost)
			{
			  IHost conn = (IHost)selectedObject;
			  if (prevProfile == null)
			    prevProfile = conn.getSystemProfile();
			  else
			    enable = (prevProfile == conn.getSystemProfile());
			  if (enable)
		        prevProfile = conn.getSystemProfile();
			}
			else
			  enable = false;
		}
		return enable;
	}
 
    // --------------------------
    // PARENT METHOD OVERRIDES...
    // --------------------------
    
	/**
	 * @see SystemBaseCopyAction#checkForCollision(Shell, IProgressMonitor, Object, Object, String)
	 */
	protected String checkForCollision(Shell shell, IProgressMonitor monitor, 
	                                   Object targetContainer, Object oldObject, String oldName)
	{
		ISystemProfile profile = (ISystemProfile)targetContainer;
		String newName = oldName;
		IHost match = sr.getHost(profile, oldName);
		if (match != null)
		{
		  //monitor.setVisible(false); wish we could!
		  //ValidatorConnectionName validator = new ValidatorConnectionName(sr.getConnectionAliasNames(profile));
		  //SystemCollisionRenameDialog dlg = new SystemCollisionRenameDialog(shell, validator, oldName);
		  SystemRenameSingleDialog dlg = new SystemRenameSingleDialog(shell, true, match, null); // true => copy-collision-mode
		  dlg.open();
		  if (!dlg.wasCancelled())
		    newName = dlg.getNewName();
		  else
		    newName = null;
		}
		return newName;
	}
	/**
	 * @see SystemBaseCopyAction#doCopy(IProgressMonitor, Object, Object, String)
	 */
	protected boolean doCopy(IProgressMonitor monitor, Object targetContainer, Object oldObject, String newName)
		throws Exception 
    {
    	IHost oldConnection = (IHost)oldObject;
    	String oldName = oldConnection.getAliasName();
    	//RSEUIPlugin.logDebugMessage(this.getClass().getName(),"starting to copy "+oldName+" to "+newName);
    	ISystemProfile targetProfile = (ISystemProfile)targetContainer;
        IHost newConn = sr.copyHost(monitor, oldConnection, targetProfile, newName);
		return (newConn != null);
	}

	/**
	 * @see SystemBaseCopyAction#getTreeModel()
	 */
	protected SystemSimpleContentElement getTreeModel() 
	{
		return getProfileTreeModel(getFirstSelectedConnection().getSystemProfile());
	}
	/**
	 * @see SystemBaseCopyAction#getTreeInitialSelection()
	 */
	protected SystemSimpleContentElement getTreeInitialSelection()
	{
		return initialSelectionElement;
	}

	/**
	 * @see SystemBaseCopyAction#getPromptString()
	 */
	protected String getPromptString() 
	{
		return SystemResources.RESID_COPY_TARGET_PROFILE_PROMPT;
	}
	/**
	 * @see SystemBaseCopyAction#getCopyingMessage()
	 */
	protected SystemMessage getCopyingMessage() 
	{
		return RSEUIPlugin.getPluginMessage(MSG_COPYCONNECTIONS_PROGRESS);
	}
	/**
	 * @see SystemBaseCopyAction#getCopyingMessage( String)
	 */
	protected SystemMessage getCopyingMessage(String oldName) 
	{
		return RSEUIPlugin.getPluginMessage(MSG_COPYCONNECTION_PROGRESS).makeSubstitution(oldName);
	}

	/**
	 * @see SystemBaseCopyAction#getOldObjects()
	 */
	protected Object[] getOldObjects() 
	{
		return getSelectedConnections();
	}

	/**
	 * @see SystemBaseCopyAction#getOldNames()
	 */
	protected String[] getOldNames() 
	{
		IHost[] conns = getSelectedConnections();
		String[] names = new String[conns.length];
		for (int idx=0; idx<conns.length; idx++)
		   names[idx] = conns[idx].getAliasName();
		return names;
	}

    /**
     * Get the currently selected connections
     */
    protected IHost[] getSelectedConnections()
    {
   	    IStructuredSelection selection = (IStructuredSelection)getSelection();
   	    IHost[] conns = new IHost[selection.size()];
   	    Iterator i = selection.iterator();
   	    int idx=0;
   	    while (i.hasNext())
   	    {
   	       conns[idx++] = (IHost)i.next();
   	    }
   	    return conns;
    }
    /**
     * Get the first selected connection
     */
    protected IHost getFirstSelectedConnection()
    {
    	return (IHost)getFirstSelection();
    }
   
    // ------------------
    // PRIVATE METHODS...
    // ------------------
    
	/**
	 * Create and return data model to populate selection tree with.
	 * @param profile whose tree model element is to be selected
	 */
    protected SystemSimpleContentElement getProfileTreeModel(ISystemProfile profile)
    {
    	SystemSimpleContentElement veryRootElement = 
    	   new SystemSimpleContentElement("Profiles",
    	                                  null, null, (Vector)null);	    	
    	veryRootElement.setRenamable(false);
    	veryRootElement.setDeletable(false);
    	                
    	ISystemProfile[] profiles = RSEUIPlugin.getTheSystemRegistry().getActiveSystemProfiles();
    	ImageDescriptor image = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_PROFILE_ID);
    	                                  
    	if (profiles == null)
    	  return veryRootElement;
    	 
    	Vector veryRootChildren = new Vector(); 
    	for (int idx=0; idx<profiles.length; idx++)
    	{
           SystemSimpleContentElement profileElement = 
    	      new SystemSimpleContentElement(profiles[idx].getName(),
    	                                     profiles[idx], veryRootElement, (Vector)null);	
    	   profileElement.setRenamable(false);
    	   profileElement.setDeletable(false);
    	   profileElement.setImageDescriptor(image);
           veryRootChildren.addElement(profileElement);
           if (profiles[idx] == profile)
             initialSelectionElement = profileElement;          
    	}    	
        veryRootElement.setChildren(veryRootChildren);    	
    	return veryRootElement;
    }
     
}