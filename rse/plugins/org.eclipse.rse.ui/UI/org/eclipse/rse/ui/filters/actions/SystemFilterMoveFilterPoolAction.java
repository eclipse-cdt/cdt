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

package org.eclipse.rse.ui.filters.actions;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.rse.core.subsystems.util.ISubsystemConfigurationAdapter;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.filters.ISystemFilterPoolManagerProvider;
import org.eclipse.rse.filters.ISystemFilterPoolReference;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.rse.ui.actions.SystemBaseCopyAction;
import org.eclipse.rse.ui.dialogs.SystemRenameSingleDialog;
import org.eclipse.rse.ui.dialogs.SystemSimpleContentElement;
import org.eclipse.swt.widgets.Shell;


/**
 * Move a filter pool action.
 */
public class SystemFilterMoveFilterPoolAction extends SystemBaseCopyAction
       implements  ISystemMessages
{
	private String promptString = null;
	private SystemSimpleContentElement initialSelectionElement = null;
	private SystemSimpleContentElement root = null;
	
	/**
	 * Constructor 
	 */
	public SystemFilterMoveFilterPoolAction(Shell parent) 
	{
		super(parent, SystemResources.ACTION_MOVE_FILTERPOOL_LABEL, MODE_MOVE);
		promptString = SystemResources.RESID_MOVE_PROMPT;		
	}

	/**
	 * Reset. This is a re-run of this action
	 */
	protected void reset()
	{
		super.reset();
		initialSelectionElement = null;
		root = null;
	}

    /**
     * Set the help context Id (infoPop) for this action. This must be fully qualified by
     *  plugin ID.
     * <p>
     * Same as {@link org.eclipse.rse.ui.actions.SystemBaseAction #setHelp(String)}
     * @see org.eclipse.rse.ui.actions.SystemBaseAction #getHelpContextId()
     */
    public void setHelpContextId(String id)
    {
    	setHelp(id);
    }
 
	/**
	 * We override from parent to do unique checking...
	 * <p>
	 * We intercept to ensure only filterpools from the same filterpool manager are selected.
	 * <p>
	 * @see SystemBaseAction#updateSelection(IStructuredSelection)
	 */
	public boolean updateSelection(IStructuredSelection selection)
	{
		boolean enable = true;
		/* */
		Iterator e= ((IStructuredSelection) selection).iterator();		
		ISystemFilterPoolManager prevMgr = null;
		ISystemFilterPoolManager currMgr = null;		
		ISystemFilterPool pool;
		while (enable && e.hasNext())
		{
			Object selectedObject = e.next();
			if (selectedObject instanceof SystemSimpleContentElement)
			  selectedObject = ((SystemSimpleContentElement)selectedObject).getData();
			if (!(selectedObject instanceof ISystemFilterPool) &&
			    !(selectedObject instanceof ISystemFilterPoolReference))
			  enable = false;
			// disable if this is a connection-unique filter pool
			else if (selectedObject instanceof ISystemFilterPool)
				enable = ((ISystemFilterPool)selectedObject).getOwningParentName() == null;
			// disable if this is a connection-unique filter pool
			else if (selectedObject instanceof ISystemFilterPoolReference)
				enable = ((ISystemFilterPoolReference)selectedObject).getReferencedFilterPool().getOwningParentName() == null;
							  
			if (enable)
			{
			  if (selectedObject instanceof ISystemFilterPool)
			    pool = (ISystemFilterPool)selectedObject;
			  else
			    pool = ((ISystemFilterPoolReference)selectedObject).getReferencedFilterPool();
			  currMgr = pool.getSystemFilterPoolManager();
			  if (prevMgr == null)
			    prevMgr = currMgr;
			  else
			    enable = (prevMgr == currMgr);
			  if (enable)
		        prevMgr = currMgr;
			}
		}
		/* */
		return enable;
	}
 
    // --------------------------
    // PARENT METHOD OVERRIDES...
    // --------------------------
	/**
	 * This method is a callback from the select-target-parent dialog, allowing us to decide whether the current selected
	 * object is a valid parent object. This affects the enabling of the OK button on that dialog.
	 */
	public boolean isValidTargetParent(SystemSimpleContentElement selectedElement)
	{
		if (selectedElement == null)
		  return false;
		Object data = selectedElement.getData();
		return (data instanceof ISystemFilterPoolManager);
	}
    
	/**
	 * @see SystemBaseCopyAction#checkForCollision(Shell, IProgressMonitor, Object, Object, String)
	 */
	protected String checkForCollision(Shell shell, IProgressMonitor monitor,
	                                   Object targetContainer, Object oldObject, String oldName)
	{
		ISystemFilterPoolManager newMgr = (ISystemFilterPoolManager)targetContainer;
		String newName = oldName;
		ISystemFilterPool match = newMgr.getSystemFilterPool(oldName);
		if (match != null)
		{
		  //monitor.setVisible(false); wish we could!
		  //ValidatorFilterPoolName validator = new ValidatorFilterPoolName(newMgr.getSystemFilterPoolNames());
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
    	ISystemFilterPool oldFilterPool = (ISystemFilterPool)oldObject;
		ISystemFilterPoolManager oldMgr = oldFilterPool.getSystemFilterPoolManager();
    	ISystemFilterPoolManager newMgr = (ISystemFilterPoolManager)targetContainer;
        ISystemFilterPool newFilterPool = oldMgr.moveSystemFilterPool(newMgr, oldFilterPool, newName);
        if ((root != null) && (newFilterPool!=null))
        {
          Object data = root.getData();
          if ((data!=null) && (data instanceof TreeViewer))
            ((TreeViewer)data).refresh();
        }
		return (newFilterPool != null);
	}

	/**
	 * @see SystemBaseCopyAction#getTreeModel()
	 */
	protected SystemSimpleContentElement getTreeModel() 
	{
		ISystemFilterPool firstPool = getFirstSelectedFilterPool(); 
		ISystemFilterPoolManagerProvider provider = firstPool.getProvider();
		
		return getPoolMgrTreeModel(provider, firstPool.getSystemFilterPoolManager());
	}
	/**
	 * @see SystemBaseCopyAction#getTreeInitialSelection()
	 */
	protected SystemSimpleContentElement getTreeInitialSelection()
	{
		return initialSelectionElement;
	}

    /**
     * Set the prompt string that shows up at the top of the copy-destination dialog.
     */
    public void setPromptString(String promptString)
    {
    	this.promptString = promptString;
    }
	/**
	 * @see SystemBaseCopyAction#getPromptString()
	 */
	protected String getPromptString() 
	{
		return promptString;
	}
	/**
	 * @see SystemBaseCopyAction#getCopyingMessage()
	 */
	protected SystemMessage getCopyingMessage() 
	{
		return RSEUIPlugin.getPluginMessage(MSG_MOVEFILTERPOOLS_PROGRESS);
	}
	/**
	 * @see SystemBaseCopyAction#getCopyingMessage( String)
	 */
	protected SystemMessage getCopyingMessage(String oldName) 
	{
		return RSEUIPlugin.getPluginMessage(MSG_MOVEFILTERPOOL_PROGRESS).makeSubstitution(oldName);
	}
	/**
	 * Return complete message
	 */
	public SystemMessage getCompletionMessage(Object targetContainer, String[] oldNames, String[] newNames)
	{
		return RSEUIPlugin.getPluginMessage(MSG_MOVEFILTERPOOL_COMPLETE).makeSubstitution(((ISystemFilterPoolManager)targetContainer).getName());		
	}

	/**
	 * @see SystemBaseCopyAction#getOldObjects()
	 */
	protected Object[] getOldObjects() 
	{
		return getSelectedFilterPools();
	}

	/**
	 * @see SystemBaseCopyAction#getOldNames()
	 */
	protected String[] getOldNames() 
	{
		ISystemFilterPool[] filterPools = getSelectedFilterPools();
		String[] names = new String[filterPools.length];
		for (int idx=0; idx<filterPools.length; idx++)
		   names[idx] = filterPools[idx].getName();
		return names;
	}

    /**
     * Get the currently selected filter pools
     */
    protected ISystemFilterPool[] getSelectedFilterPools()
    {
   	    IStructuredSelection selection = (IStructuredSelection)getSelection();
   	    ISystemFilterPool[] filterPools = new ISystemFilterPool[selection.size()];
   	    Iterator i = selection.iterator();
   	    int idx=0;
   	    while (i.hasNext())
   	    {
   	       Object next = i.next();
   	       if (next instanceof SystemSimpleContentElement)    	
   	         next = ((SystemSimpleContentElement)next).getData();
   	       if (next instanceof ISystemFilterPoolReference)
   	         filterPools[idx++] = ((ISystemFilterPoolReference)next).getReferencedFilterPool();
   	       else
   	         filterPools[idx++] = (ISystemFilterPool)next;
   	    }
   	    return filterPools;
    }
    /**
     * Get the managers of the currently selected filter pools
     */
    protected ISystemFilterPoolManager[] getSelectedFilterPoolManagers()
    {
    	ISystemFilterPoolManager[] mgrs = null;
    	Vector v = new Vector();
    	ISystemFilterPool[] pools = getSelectedFilterPools();    	
    	for (int idx=0; idx<pools.length; idx++)
    	{
    	   ISystemFilterPoolManager mgr = pools[idx].getSystemFilterPoolManager();
    	   if (!v.contains(mgr))
    	     v.addElement(mgr);  
    	}
    	
    	mgrs = new ISystemFilterPoolManager[v.size()];
    	for (int idx=0; idx<mgrs.length; idx++)
    	   mgrs[idx] = (ISystemFilterPoolManager)v.elementAt(idx);
    	
    	return mgrs;
    }

    /**
     * Get the first selected filter pool
     */
    protected ISystemFilterPool getFirstSelectedFilterPool()
    {
    	Object first = getFirstSelection();
   	    if (first instanceof SystemSimpleContentElement)    	
   	    {
   	      root = ((SystemSimpleContentElement)first).getRoot();
   	      first = ((SystemSimpleContentElement)first).getData();
   	    }
   	    if (first == null)
   	      return null;
   	    else if (first instanceof ISystemFilterPoolReference)
   	      return ((ISystemFilterPoolReference)first).getReferencedFilterPool();
   	    else if (first instanceof ISystemFilterPool)
   	      return (ISystemFilterPool)first;
   	    else
   	      return null;   	      
    }
   
    // ------------------
    // PRIVATE METHODS...
    // ------------------
    
	/**
	 * Create and return data model to populate selection tree with.
	 * @param poolMgrProvider The provider who will give us the list of filter pool managers to populate the list with
	 * @param poolMgr The current SystemFilterPoolManager which is to be excluded from the list.
	 */
    protected SystemSimpleContentElement getPoolMgrTreeModel(ISystemFilterPoolManagerProvider poolMgrProvider, ISystemFilterPoolManager poolMgr)
    {
    	SystemSimpleContentElement veryRootElement = 
    	   new SystemSimpleContentElement("Root",
    	                                  null, null, (Vector)null);	    	
    	veryRootElement.setRenamable(false);
    	veryRootElement.setDeletable(false);
    	                
    	ISystemFilterPoolManager[] mgrs = poolMgrProvider.getSystemFilterPoolManagers();
    	
    	ISubsystemConfigurationAdapter adapter = (ISubsystemConfigurationAdapter)poolMgrProvider.getAdapter(ISubsystemConfigurationAdapter.class);
    	ImageDescriptor image = adapter.getSystemFilterPoolManagerImage();
    	                                  
    	if ((mgrs == null) || (mgrs.length == 0))
    	  return veryRootElement;
    	 
    	Vector veryRootChildren = new Vector(); 
    	for (int idx=0; idx<mgrs.length; idx++)
    	{
    	   if (mgrs[idx] != poolMgr)
    	   {
             SystemSimpleContentElement mgrElement = 
    	        new SystemSimpleContentElement(mgrs[idx].getName(),
    	                                       mgrs[idx], veryRootElement, (Vector)null);	
    	     mgrElement.setRenamable(false);
    	     mgrElement.setDeletable(false);
    	     mgrElement.setImageDescriptor(image);
             veryRootChildren.addElement(mgrElement);
             if (initialSelectionElement == null)
               initialSelectionElement = mgrElement;          
    	   }
    	}    	
        veryRootElement.setChildren(veryRootChildren);    	
    	return veryRootElement;
    }

}