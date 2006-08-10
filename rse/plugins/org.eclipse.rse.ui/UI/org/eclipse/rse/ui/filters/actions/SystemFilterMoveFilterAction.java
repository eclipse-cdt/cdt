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
import org.eclipse.rse.core.subsystems.util.ISubSystemConfigurationAdapter;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.filters.ISystemFilterPoolManagerProvider;
import org.eclipse.rse.filters.ISystemFilterReference;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.rse.ui.actions.SystemBaseCopyAction;
import org.eclipse.rse.ui.dialogs.SystemRenameSingleDialog;
import org.eclipse.rse.ui.dialogs.SystemSimpleContentElement;
import org.eclipse.rse.ui.filters.SystemFilterUIHelpers;
import org.eclipse.swt.widgets.Shell;


/**
 * Copy a filter action.
 */
public class SystemFilterMoveFilterAction extends SystemBaseCopyAction
       implements  ISystemMessages
{
	private String promptString = null;
	private SystemSimpleContentElement initialSelectionElement = null;
	private SystemSimpleContentElement root = null;
	
	/**
	 * Constructor 
	 */
	public SystemFilterMoveFilterAction(Shell parent) 
	{
		super(parent, SystemResources.ACTION_MOVE_FILTER_LABEL, MODE_MOVE);
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
	 * @see SystemBaseAction#updateSelection(IStructuredSelection)
	 */
	public boolean updateSelection(IStructuredSelection selection)
	{
		boolean enable = true;
		/* */
		Iterator e= ((IStructuredSelection) selection).iterator();		
		while (enable && e.hasNext())
		{
			Object selectedObject = e.next();
			if (selectedObject instanceof SystemSimpleContentElement)
			  selectedObject = ((SystemSimpleContentElement)selectedObject).getData();
			if (!checkObjectType(selectedObject))
			  enable = false;
		}
		/* */
		return enable;
	}
    /**
     * Called by SystemBaseAction when selection is set.
     * Our opportunity to verify we are allowed for this selected type.
     */
	public boolean checkObjectType(Object selectedObject)
	{
		if (selectedObject instanceof ISystemFilter) 
		{
			ISystemFilter fs = (ISystemFilter)selectedObject;
			return !fs.isNonChangable();
		}
		else if (selectedObject instanceof ISystemFilterReference) 
		{
			ISystemFilter fs = ((ISystemFilterReference)selectedObject).getReferencedFilter();
			return !fs.isNonChangable();
		}
        else
          return false;
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
		return (data instanceof ISystemFilterPool);
	}
    
	/**
	 * @see SystemBaseCopyAction#checkForCollision(Shell, IProgressMonitor, Object, Object, String)
	 */
	protected String checkForCollision(Shell shell, IProgressMonitor monitor,
	                                   Object targetContainer, Object oldObject, String oldName)
	{
		ISystemFilterPool newPool = (ISystemFilterPool)targetContainer;
		ISystemFilterPoolManager newMgr = newPool.getSystemFilterPoolManager();
		String newName = oldName;
		ISystemFilter match = newPool.getSystemFilter(oldName);
		if (match != null)
		{
		  //monitor.setVisible(false); wish we could!
		  //ValidatorFilterName validator = new ValidatorFilterName(newPool.getSystemFilterNames());
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
    	ISystemFilter         oldFilter = (ISystemFilter)oldObject;
    	ISystemFilterPool oldFilterPool = oldFilter.getParentFilterPool();
		ISystemFilterPoolManager oldMgr = oldFilterPool.getSystemFilterPoolManager();
    	ISystemFilterPool       newPool = (ISystemFilterPool)targetContainer;
    	ISystemFilterPoolManager newMgr = newPool.getSystemFilterPoolManager();
    	
        ISystemFilter         newFilter = oldMgr.moveSystemFilter(newPool, oldFilter, newName);
        
        if ((root != null) && (newFilter!=null))
        {
          	Object data = root.getData();
          	if ((data!=null) && (data instanceof TreeViewer))
            	((TreeViewer)data).refresh();
        }
		return (newFilter != null);
	}

	/**
	 * @see SystemBaseCopyAction#getTreeModel()
	 */
	protected SystemSimpleContentElement getTreeModel() 
	{
		ISystemFilter firstFilter = getFirstSelectedFilter(); 
		ISystemFilterPoolManagerProvider provider = firstFilter.getProvider();
		return getPoolMgrTreeModel(provider, firstFilter.getSystemFilterPoolManager(), firstFilter.getParentFilterPool());
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
		return RSEUIPlugin.getPluginMessage(MSG_MOVEFILTERS_PROGRESS);
	}
	/**
	 * @see SystemBaseCopyAction#getCopyingMessage( String)
	 */
	protected SystemMessage getCopyingMessage(String oldName) 
	{
		return RSEUIPlugin.getPluginMessage(MSG_MOVEFILTER_PROGRESS).makeSubstitution(oldName);
	}

	/**
	 * @see SystemBaseCopyAction#getOldObjects()
	 */
	protected Object[] getOldObjects() 
	{
		return getSelectedFilters();
	}

	/**
	 * @see SystemBaseCopyAction#getOldNames()
	 */
	protected String[] getOldNames() 
	{
		ISystemFilter[] filters = getSelectedFilters();
		String[] names = new String[filters.length];
		for (int idx=0; idx<filters.length; idx++)
		   names[idx] = filters[idx].getName();
		return names;
	}

    /**
     * Get the currently selected filters
     */
    protected ISystemFilter[] getSelectedFilters()
    {
   	    IStructuredSelection selection = (IStructuredSelection)getSelection();
   	    ISystemFilter[] filters = new ISystemFilter[selection.size()];
   	    Iterator i = selection.iterator();
   	    int idx=0;
   	    while (i.hasNext())
   	    {
   	       Object next = i.next();
   	       if (next instanceof SystemSimpleContentElement)    	
   	         next = ((SystemSimpleContentElement)next).getData();
   	       if (next instanceof ISystemFilterReference)
   	         filters[idx++] = ((ISystemFilterReference)next).getReferencedFilter();
   	       else
   	         filters[idx++] = (ISystemFilter)next;
   	    }
   	    return filters;
    }
    /**
     * Get the first selected filter
     */
    protected ISystemFilter getFirstSelectedFilter()
    {
    	Object first = getFirstSelection();
   	    if (first instanceof SystemSimpleContentElement)    	
   	    {
   	      root = ((SystemSimpleContentElement)first).getRoot();
   	      first = ((SystemSimpleContentElement)first).getData();
   	    }
   	    if (first == null)
   	      return null;
   	    else if (first instanceof ISystemFilterReference)
   	      return ((ISystemFilterReference)first).getReferencedFilter();
   	    else if (first instanceof ISystemFilter)
   	      return (ISystemFilter)first;
   	    else
   	      return null;   	      
    }
   
    // ------------------
    // PRIVATE METHODS...
    // ------------------
    
	/**
	 * Create and return data model to populate selection tree with.
	 * @param poolMgrProvider The provider who will give us the list of filter pool managers to populate the list with
	 * @param poolMgr The SystemFilterPoolManager whose tree model element is to be pre-selected
	 * @param pool The SystemFilterPool whose tree model element is to be excluded
	 */
    protected SystemSimpleContentElement getPoolMgrTreeModel(ISystemFilterPoolManagerProvider poolMgrProvider, 
                                                             ISystemFilterPoolManager poolMgr,
                                                             ISystemFilterPool pool)
    {
    	SystemSimpleContentElement veryRootElement = 
    	   new SystemSimpleContentElement("Root",
    	                                  null, null, (Vector)null);	    	
    	veryRootElement.setRenamable(false);
    	veryRootElement.setDeletable(false);
    	                
    	ISystemFilterPoolManager[] mgrs = poolMgrProvider.getSystemFilterPoolManagers();

    	ISubSystemConfigurationAdapter adapter = (ISubSystemConfigurationAdapter)poolMgrProvider.getAdapter(ISubSystemConfigurationAdapter.class);
    	ImageDescriptor image = adapter.getSystemFilterPoolManagerImage();
    	                                  
    	if ((mgrs == null) || (mgrs.length == 0))
    	  return veryRootElement;

    	Vector veryRootChildren = new Vector(); 
    	for (int idx=0; idx<mgrs.length; idx++)
    	{
           SystemSimpleContentElement mgrElement = 
    	      new SystemSimpleContentElement(mgrs[idx].getName(),
    	                                     mgrs[idx], veryRootElement, (Vector)null);	
    	   mgrElement.setRenamable(false);
    	   mgrElement.setDeletable(false);
    	   mgrElement.setImageDescriptor(image);
    	   
    	   Vector elements = new Vector();    	   
           populateFilterPoolContentElementVector(poolMgrProvider, mgrs[idx], elements, mgrElement, pool);    	   
           mgrElement.setChildren(elements);

           if (mgrs[idx] == poolMgr)
             initialSelectionElement = mgrElement;          
    	   
           veryRootChildren.addElement(mgrElement);
    	}    	
        veryRootElement.setChildren(veryRootChildren);    	
    	return veryRootElement;
    }
    
    /**
     * Populate filter pool manager subtree with filter pools
     */
    protected static void populateFilterPoolContentElementVector(ISystemFilterPoolManagerProvider poolMgrProvider,
                                                                 ISystemFilterPoolManager mgr, 
                                                                 Vector elements, 
                                                                 SystemSimpleContentElement parentElement,
                                                                 ISystemFilterPool poolToExclude)
    {
    	ISystemFilterPool[] pools = mgr.getSystemFilterPools();
        for (int idx=0; idx<pools.length; idx++)
        {          
           ISystemFilterPool pool = pools[idx];
           if (pool != poolToExclude)
           {
             SystemSimpleContentElement cElement = 
               new SystemSimpleContentElement(pool.getName(), pool, parentElement, (Vector)null);
             cElement.setImageDescriptor(SystemFilterUIHelpers.getFilterPoolImage(poolMgrProvider, pool));
             //cElement.setSelected(setFilterPoolSelection(pool));           
             elements.addElement(cElement);  
           }
        }        
    }
    
     
}