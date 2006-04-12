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
import org.eclipse.rse.filters.ISystemFilterPoolReference;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManager;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.swt.widgets.Shell;


/**
 * The action allows users to remove a filter pool reference
 */
public class SystemFilterRemoveFilterPoolReferenceAction 
       extends SystemBaseAction 
       
{


	/**
	 * Constructor
	 */
	public SystemFilterRemoveFilterPoolReferenceAction(Shell parent) 
	{
		super(SystemResources.ACTION_RMVFILTERPOOLREF_LABEL,SystemResources.ACTION_RMVFILTERPOOLREF_TOOLTIP,
		      RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_DELETEREF_ID),
		      parent);
        allowOnMultipleSelection(true);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_REORGANIZE); 
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
     * Called by SystemBaseAction when selection is set.
     * Our opportunity to verify we are allowed for this selected type.
     */
	public boolean checkObjectType(Object selectedObject)
	{
		if (!(selectedObject instanceof ISystemFilterPoolReference))
		  	return false;
		// disable if this is a connection-unique filter pool
		else 
			return ((ISystemFilterPoolReference)selectedObject).getReferencedFilterPool().getOwningParentName() == null;
	}
	
	/**
	 * This is the method called when the user selects this action.
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() 
	{
		IStructuredSelection selections = getSelection();
		ISystemFilterPoolReference poolReferences[] = new ISystemFilterPoolReference[selections.size()];
		Iterator i = selections.iterator();
		ISystemFilterPoolReferenceManager fprMgr = null;
		while (i.hasNext())		
		{
		  ISystemFilterPoolReference poolReference = (ISystemFilterPoolReference)i.next();
		  fprMgr = poolReference.getFilterPoolReferenceManager();
		  fprMgr.removeSystemFilterPoolReference(poolReference,true); // true means do dereference
		}
	}		
}