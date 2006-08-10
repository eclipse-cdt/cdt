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
import org.eclipse.rse.filters.ISystemFilterString;
import org.eclipse.rse.filters.ISystemFilterStringReference;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.rse.ui.actions.SystemBaseCopyAction;
import org.eclipse.rse.ui.dialogs.SystemRenameSingleDialog;
import org.eclipse.rse.ui.dialogs.SystemSimpleContentElement;
import org.eclipse.rse.ui.filters.SystemFilterUIHelpers;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.validators.ValidatorUniqueString;
import org.eclipse.swt.widgets.Shell;


/**
 * Move a filter string action.
 */
public class SystemFilterMoveFilterStringAction extends SystemBaseCopyAction
       implements  ISystemMessages
{
	private String promptString = null;
	private SystemSimpleContentElement initialSelectionElement = null;
	private SystemSimpleContentElement root = null;
	private ISystemFilterString[] strings = null;
	
	/**
	 * Constructor 
	 */
	public SystemFilterMoveFilterStringAction(Shell parent) 
	{
		super(parent, SystemResources.ACTION_MOVE_FILTERSTRING_LABEL, MODE_MOVE);
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
		strings = null;
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
     * Overridable method if you support duplicate filter strings in the same filter.
     * By default, queries the "supportsDuplicateFilterStrings" attribute in the filter.
     */
    protected boolean supportsDuplicateFilterStrings(ISystemFilter filter)
    {
    	return filter.isSupportsDuplicateFilterStrings();
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
		if (selectedObject instanceof ISystemFilterString) 
		{
			ISystemFilterString fs = (ISystemFilterString)selectedObject;
			return fs.isChangable();
		}
		else if (selectedObject instanceof ISystemFilterStringReference) 
		{
			ISystemFilterStringReference frs = (ISystemFilterStringReference)selectedObject;
			return frs.getReferencedFilterString().isChangable();
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
	 * <p>
	 * The default is to return true if the selected element has no children. This is sufficient for most cases. However, 
	 * in some cases it is not, such as for filter strings where we want to only enable OK if a filter is selected. It is 
	 * possible that filter pools have no filters, so the default algorithm is not sufficient. In these cases the child class
	 * can override this method.
	 */
	public boolean isValidTargetParent(SystemSimpleContentElement selectedElement)
	{
		Object data = selectedElement.getData();
		if (data instanceof ISystemFilter)
		  return !((ISystemFilter)data).isPromptable();
		else
		  return false;
	}

	/**
	 * Overridable entry point when you want to prevent any copies/moves if any of the
	 * selected objects have a name collision.
	 * <p>
	 * If you decide to override this, it is your responsibility to issue the error 
	 * message to the user and return false here.
	 * <p>
	 * @return true if there is no problem, false if there is a fatal collision
	 */
	protected boolean preCheckForCollision(Shell shell, Object targetContainer, 
	                                       Object oldObject, String oldName)
	{
		ISystemFilter newFilter = (ISystemFilter)targetContainer;
		if (supportsDuplicateFilterStrings(newFilter))
		  return true;
		ISystemFilterString match = newFilter.getSystemFilterString(oldName);
		if (match != null)
		{
		  SystemMessage msg = RSEUIPlugin.getPluginMessage(MSG_VALIDATE_FILTERSTRING_ALREADYEXISTS);
		  msg.makeSubstitution(oldName, newFilter.getName());
  		  SystemMessageDialog.displayErrorMessage(shell, msg);
  		  
		}
		return (match == null); // all is well iff such a filter string doesn't already exist.
	}	
    
	/**
	 * SHOULD NEVER BE CALLED IF preCheckForCollision WORKS PROPERLY
	 * @see SystemBaseCopyAction#checkForCollision(Shell, IProgressMonitor, Object, Object, String)
	 */
	protected String checkForCollision(Shell shell, IProgressMonitor monitor,
	                                   Object targetContainer, Object oldObject, String oldName)
	{
		ISystemFilter     newFilter = (ISystemFilter)targetContainer;
		if (supportsDuplicateFilterStrings(newFilter))
		  return oldName;
		ISystemFilterPool newPool   = newFilter.getParentFilterPool();
		ISystemFilterPoolManager newMgr = newPool.getSystemFilterPoolManager();
		String newName = oldName;
		ISystemFilterString match = newFilter.getSystemFilterString(oldName);
		if (match != null)
		{
		  //monitor.setVisible(false); wish we could!
		  boolean caseSensitive = false;
		  ValidatorUniqueString validator = new ValidatorUniqueString(newFilter.getFilterStrings(),caseSensitive);
		  //SystemCollisionRenameDialog dlg = new SystemCollisionRenameDialog(shell, validator, oldName);
		  SystemRenameSingleDialog dlg = new SystemRenameSingleDialog(shell, true, match, validator); // true => copy-collision-mode
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
    	ISystemFilterString oldFilterString = (ISystemFilterString)oldObject;
		ISystemFilterPoolManager     oldMgr = oldFilterString.getSystemFilterPoolManager();
    	ISystemFilter          targetFilter = (ISystemFilter)targetContainer;
    	//SystemFilterPoolManager     newMgr = targetFilter.getSystemFilterPoolManager();
    	
        ISystemFilterString newFilterString = oldMgr.moveSystemFilterString(targetFilter, oldFilterString);
        
        if ((root != null) && (newFilterString!=null))
        {
          Object data = root.getData();
          if ((data!=null) && (data instanceof TreeViewer))
            ((TreeViewer)data).refresh();
        }
		return (newFilterString != null);
	}

	/**
	 * @see SystemBaseCopyAction#getTreeModel()
	 */
	protected SystemSimpleContentElement getTreeModel() 
	{
		ISystemFilterString firstFilterString = getFirstSelectedFilterString(); 
		ISystemFilterPoolManagerProvider provider = firstFilterString.getProvider();
		return getPoolMgrTreeModel(provider, firstFilterString.getSystemFilterPoolManager(), getSelectedFilters());
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
		return RSEUIPlugin.getPluginMessage(MSG_MOVEFILTERSTRINGS_PROGRESS);
	}
	/**
	 * @see SystemBaseCopyAction#getCopyingMessage( String)
	 */
	protected SystemMessage getCopyingMessage(String oldName) 
	{
		return RSEUIPlugin.getPluginMessage(MSG_MOVEFILTERSTRING_PROGRESS).makeSubstitution(oldName);
	}

	/**
	 * @see SystemBaseCopyAction#getOldObjects()
	 */
	protected Object[] getOldObjects() 
	{
		return getSelectedFilterStrings();
	}

	/**
	 * @see SystemBaseCopyAction#getOldNames()
	 */
	protected String[] getOldNames() 
	{
		ISystemFilterString[] strings = getSelectedFilterStrings();
		String[] names = new String[strings.length];
		for (int idx=0; idx<strings.length; idx++)
		   names[idx] = strings[idx].getString();
		return names;
	}

    /**
     * Get the currently selected filter strings
     */
    protected ISystemFilterString[] getSelectedFilterStrings()
    {
    	if (strings == null)
    	{
   	      IStructuredSelection selection = (IStructuredSelection)getSelection();
   	      strings = new ISystemFilterString[selection.size()];
   	      Iterator i = selection.iterator();
   	      int idx=0;
   	      while (i.hasNext())
   	      {
   	         Object next = i.next();
   	         if (next instanceof SystemSimpleContentElement)    	
   	           next = ((SystemSimpleContentElement)next).getData();
   	         if (next instanceof ISystemFilterStringReference)
   	           strings[idx++] = ((ISystemFilterStringReference)next).getReferencedFilterString();
   	         else
   	           strings[idx++] = (ISystemFilterString)next;
   	      }
   	    }
   	    return strings;
    }
    /**
     * Get the intersection list of filters of currently selected filter strings
     */
    protected ISystemFilter[] getSelectedFilters()
    {
        Vector v = new Vector();
        ISystemFilterString[] strings = getSelectedFilterStrings();
        for (int idx=0; idx<strings.length; idx++)
        {
           ISystemFilter filter = strings[idx].getParentSystemFilter();
		   if (!supportsDuplicateFilterStrings(filter) &&
               !SystemFilterCopyFilterStringAction.containsFilter(v,filter))
             v.addElement(filter);      	
        }
        ISystemFilter[] filters = new ISystemFilter[v.size()];
        for (int idx=0; idx<v.size(); idx++)
           filters[idx] = (ISystemFilter)v.elementAt(idx);
        return filters;
    }    
    /**
     * Get the first selected filter string
     */
    protected ISystemFilterString getFirstSelectedFilterString()
    {
    	Object first = getFirstSelection();
   	    if (first instanceof SystemSimpleContentElement)    	
   	    {
   	      root = ((SystemSimpleContentElement)first).getRoot();
   	      first = ((SystemSimpleContentElement)first).getData();
   	    }
   	    if (first == null)
   	      return null;
   	    else if (first instanceof ISystemFilterStringReference)
   	      return ((ISystemFilterStringReference)first).getReferencedFilterString();
   	    else if (first instanceof ISystemFilterString)
   	      return (ISystemFilterString)first;
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
	 * @param filtersToExclude All the currently selected filters, which are excluded from the target list
	 */
    protected SystemSimpleContentElement getPoolMgrTreeModel(ISystemFilterPoolManagerProvider poolMgrProvider, 
                                                             ISystemFilterPoolManager poolMgr,
                                                             ISystemFilter[] filtersToExclude)
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

        ISystemFilter parentFilter = getFirstSelectedFilterString().getParentSystemFilter();
        ISystemFilterPool parentFilterPool = parentFilter.getParentFilterPool();
         
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
           populateFilterPoolContentElementVector(poolMgrProvider, mgrs[idx], elements, 
                                                  mgrElement, filtersToExclude, 
                                                  parentFilterPool, parentFilter);   
           mgrElement.setChildren(elements);
    	   
           veryRootChildren.addElement(mgrElement);
           //if (mgrs[idx] == poolMgr)
             //initialSelectionElement = mgrElement;          
    	}    	
        veryRootElement.setChildren(veryRootChildren);    	
    	return veryRootElement;
    }
    
    /**
     * Populate filter pool manager subtree with filter pools
     */
    protected void populateFilterPoolContentElementVector(ISystemFilterPoolManagerProvider poolMgrProvider, 
                                                          ISystemFilterPoolManager mgr, 
                                                          Vector elements, 
                                                          SystemSimpleContentElement parentElement,
                                                          ISystemFilter[] filtersToExclude,
                                                          ISystemFilterPool filterPoolToSelect,
                                                          ISystemFilter filterToSelect)
    {
    	ISystemFilterPool[] pools = mgr.getSystemFilterPools();
        for (int idx=0; idx<pools.length; idx++)
        {
           ISystemFilterPool pool = pools[idx];
           SystemSimpleContentElement cElement = 
             new SystemSimpleContentElement(pool.getName(), pool, parentElement, (Vector)null);
           cElement.setImageDescriptor(SystemFilterUIHelpers.getFilterPoolImage(poolMgrProvider,pool));

           Vector childElements = new Vector();
           populateFilterContentElementVector(poolMgrProvider, pool, childElements, cElement, filtersToExclude, filterToSelect);    	   
           cElement.setChildren(childElements);           
           
           elements.addElement(cElement);             
           if ((pool == filterPoolToSelect) && (initialSelectionElement==null))
             initialSelectionElement = cElement;
        }        
    }
    
    /**
     * Populate filter pool subtree with filters
     */
    protected void populateFilterContentElementVector(ISystemFilterPoolManagerProvider poolMgrProvider, 
                                                      ISystemFilterPool pool, 
                                                      Vector elements, 
                                                      SystemSimpleContentElement parentElement,
                                                      ISystemFilter[] filtersToExclude,
                                                      ISystemFilter filterToSelect)
    {
    	ISystemFilter[] filters = pool.getSystemFilters();
        for (int idx=0; idx<filters.length; idx++)
        {
           ISystemFilter filter = filters[idx];
           if (!filter.isNonChangable() && !filter.isPromptable() && // defect 43242
               !containsFilter(filtersToExclude,filter))
           {
             SystemSimpleContentElement cElement = 
               new SystemSimpleContentElement(filter.getName(), filter, parentElement, (Vector)null);
             cElement.setImageDescriptor(SystemFilterUIHelpers.getFilterImage(poolMgrProvider,filter));
             elements.addElement(cElement);  
             if (filter == filterToSelect)
               initialSelectionElement = cElement;
           }
        }        
    }
    
    private boolean containsFilter(ISystemFilter[] filters, ISystemFilter filter)
    {
    	ISystemFilter match = null;
    	for (int idx=0; (match==null) && (idx<filters.length); idx++)
    	   if (filters[idx] == filter)
    	     match = filters[idx];
        return (match != null);
    }
     
}