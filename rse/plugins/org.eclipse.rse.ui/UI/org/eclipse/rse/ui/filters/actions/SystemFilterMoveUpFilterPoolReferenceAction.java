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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.filters.ISystemFilterPoolReference;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManager;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemSortableSelection;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.swt.widgets.Shell;


/**
 * The action allows users to move the current filter pool reference up in the list
 */
public class SystemFilterMoveUpFilterPoolReferenceAction extends SystemBaseAction 
                                 
{


	/**
	 * Constructor
	 */
	public SystemFilterMoveUpFilterPoolReferenceAction(Shell parent) 
	{
		super(SystemResources.ACTION_MOVEUP_LABEL,SystemResources.ACTION_MOVEUP_TOOLTIP,
		      SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_MOVEUP_ID),
		      parent);
        allowOnMultipleSelection(true);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_REORDER);        
	}

    /**
     * Set the help context Id (infoPop) for this action. This must be fully qualified by
     *  plugin ID.
     * <p>
     * Same as {@link org.eclipse.rse.ui.actions.SystemBaseAction#setHelp(String)}
     * @see org.eclipse.rse.ui.actions.SystemBaseAction#getHelpContextId()
     */
    public void setHelpContextId(String id)
    {
    	setHelp(id);
    }

	/**
	 * Intercept of parent method. We need to test that the filter pools
	 *  come from the same parent
	 */
	public boolean updateSelection(IStructuredSelection selection)
	{
		ISystemFilterPoolReferenceManager prevMgr = null;
		boolean enable = true;
		Iterator e= ((IStructuredSelection) selection).iterator();
		while (enable && e.hasNext())
		{
			Object selectedObject = e.next();
			ISystemFilterPoolReference filterPoolRef = (ISystemFilterPoolReference)selectedObject;
			if (prevMgr != null)
			{
				if (prevMgr != filterPoolRef.getFilterPoolReferenceManager())
					enable = false;
				else
					prevMgr = filterPoolRef.getFilterPoolReferenceManager();			 
			}
			else
				prevMgr = filterPoolRef.getFilterPoolReferenceManager();			
			if (enable)
				enable = checkObjectType(filterPoolRef);		  
		}
		return enable;
	}
	
    /**
     * Called by SystemBaseAction when selection is set.
     * Our opportunity to verify we are allowed for this selected type.
     */
	public boolean checkObjectType(Object selectedObject)
	{
		if (!(selectedObject instanceof ISystemFilterPoolReference))
		  return false;
		ISystemFilterPoolReference filterPoolRef = (ISystemFilterPoolReference)selectedObject;
		ISystemFilterPoolReferenceManager fprMgr = filterPoolRef.getFilterPoolReferenceManager();
		int pos = fprMgr.getSystemFilterPoolReferencePosition(filterPoolRef);		
		return (pos>0);
	}
	
	/**
	 * This is the method called when the user selects this action.
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() 
	{
		IStructuredSelection selections = getSelection();
		//SystemFilterPoolReference filterPoolRefs[] = new SystemFilterPoolReference[selections.size()];
		//Iterator i = selections.iterator();
		//int idx = 0;
		//while (i.hasNext())		
		  //filterPoolRefs[idx++] = (SystemFilterPoolReference)i.next();

		SystemSortableSelection[] sortableArray = new SystemSortableSelection[selections.size()];
		Iterator i = selections.iterator();
		int idx = 0;
		ISystemFilterPoolReference filterPoolRef = null;
		ISystemFilterPoolReferenceManager fprMgr = null;
		while (i.hasNext())	
		{
		  	sortableArray[idx] = new SystemSortableSelection((ISystemFilterPoolReference)i.next());
		  	filterPoolRef = (ISystemFilterPoolReference)sortableArray[idx].getSelectedObject();
		  	fprMgr = filterPoolRef.getFilterPoolReferenceManager();
		  	sortableArray[idx].setPosition(fprMgr.getSystemFilterPoolReferencePosition(filterPoolRef));
		  	idx++;
		}
		SystemSortableSelection.sortArray(sortableArray);
		ISystemFilterPoolReference[] filterPoolRefs = (ISystemFilterPoolReference[])SystemSortableSelection.getSortedObjects(sortableArray, new ISystemFilterPoolReference[sortableArray.length]);
		
		if (idx > 0)
        {
		  	fprMgr = filterPoolRefs[0].getFilterPoolReferenceManager();
		  	fprMgr.moveSystemFilterPoolReferences(filterPoolRefs,-1);
        }
	}		
}