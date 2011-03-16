/*******************************************************************************
 * Copyright (c) 2002, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [182454] improve getAbsoluteName() documentation
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Xuan Chen        (IBM) - [223126] [api][breaking] Remove API related to User Actions in RSE Core/UI
 * David McKnight   (IBM)        - [330398] RSE leaks SWT resources
 *******************************************************************************/

package org.eclipse.rse.internal.ui.view;

import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.subsystems.ISystemDragDropAdapter;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.actions.SystemPasteFromClipboardAction;
import org.eclipse.rse.ui.internal.model.SystemScratchpad;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.view.AbstractSystemViewAdapter;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.swt.widgets.Shell;



/**
 * Adapter for the root-providing object of the SystemView tree viewer.
 */
public class SystemViewScratchpadAdapter extends AbstractSystemViewAdapter implements ISystemRemoteElementAdapter,ISystemDragDropAdapter
{

	private SystemPasteFromClipboardAction _pasteToScratchpadAction = null;

	public SystemViewScratchpadAdapter()
	{
	}
	
	/**
	 * Returns any actions that should be contributed to the popup menu
	 * for the given element.
	 * @param menu The menu to contribute actions to
	 * @param selection The window's current selection.
	 * @param shell Shell of viewer
	 * @param menuGroup recommended menu group to add actions to. If added to another group, you must be sure to create that group first.
	 */
	public void addActions(SystemMenuManager menu, IStructuredSelection selection, Shell shell, String menuGroup)
	{
		if (_pasteToScratchpadAction == null)
		{
		    _pasteToScratchpadAction = new SystemPasteFromClipboardAction(shell, null);
		}
		menu.add(menuGroup, _pasteToScratchpadAction);
	}
	
	/**
	 * Returns an image descriptor for the image. More efficient than getting the image.
	 * @param element The element for which an image is desired
	 */
	public ImageDescriptor getImageDescriptor(Object element)
	{
		return null;
	}
	
	/**
	 * Return the label for this object
	 */
	public String getText(Object element)
	{
		return SystemViewResources.RESID_REMOTE_SCRATCHPAD;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IRemoteObjectIdentifier#getAbsoluteName(java.lang.Object)
	 */
	public String getAbsoluteName(Object element)
	{
		return getText(element);
	}			
	/**
	 * Return the type label for this object
	 */
	public String getType(Object element)
	{
		return SystemViewResources.RESID_SCRATCHPAD;
	}	
	
	/**
	 * Return the parent of this object
	 */
	public Object getParent(Object element)
	{
		return null;
	}
	
	/**
	 * Return the children of this object
	 */
	public Object[] getChildren(IAdaptable element, IProgressMonitor monitor)
	{
	    SystemScratchpad pad = (SystemScratchpad)element;
	    return pad.getChildren();
	}
	
	/**
	 * Return true if this object has children
	 */
	public boolean hasChildren(IAdaptable element)
	{
	    SystemScratchpad pad = (SystemScratchpad)element;
	    return pad.hasChildren();
	}
	
	// FOR COMMON DELETE ACTIONS	
	/**
	 * We don't support delete at all.
	 */
	public boolean showDelete(Object element)
	{
		return false;
	}	

	// FOR COMMON RENAME ACTIONS	
	/**
	 * We don't support rename at all.
	 */
	public boolean showRename(Object element)
	{
		return false;
	}		
	/**
	 * Return a validator for verifying the new name is correct.
	 */
    public ISystemValidator getNameValidator(Object element)
    {
    	return null;
    }	
    
   
	/**
	 * Return our unique property descriptors
	 */
	protected org.eclipse.ui.views.properties.IPropertyDescriptor[] internalGetPropertyDescriptors()
	{
		return null;
	}
	/**
	 * Return our unique property values
	 */
	public Object internalGetPropertyValue(Object key)
	{
		return null;
	}	


	public boolean canDrop(Object element)
	{
		return true;		
	}
	
	public Object doDrop(Object from, Object to, boolean sameSystemType, boolean sameSystem, int srcType, IProgressMonitor monitor)
	{
	    if (to instanceof SystemScratchpad)
	    {
	    	if (from instanceof List)
	    	{
	    		List set = (List)from;
	    		for (int i = 0;i < set.size(); i++)
	    		{
	    			((SystemScratchpad)to).addChild(set.get(i));
	    		}
	    	}
	    	else
	    	{
	    		((SystemScratchpad)to).addChild(from);
	    	}
	        return from;
	    }
	    return null;
	}

    /* (non-Javadoc)
     * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getAbsoluteParentName(java.lang.Object)
     */
    public String getAbsoluteParentName(Object element)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getSubSystemConfigurationId(java.lang.Object)
     */
    public String getSubSystemConfigurationId(Object element)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteTypeCategory(java.lang.Object)
     */
    public String getRemoteTypeCategory(Object element)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteType(java.lang.Object)
     */
    public String getRemoteType(Object element)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteSubType(java.lang.Object)
     */
    public String getRemoteSubType(Object element)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#refreshRemoteObject(java.lang.Object, java.lang.Object)
     */
    public boolean refreshRemoteObject(Object oldElement, Object newElement)
    {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteParent(org.eclipse.swt.widgets.Shell, java.lang.Object)
     */
    public Object getRemoteParent(Object element, IProgressMonitor monitor) throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteParentNamesInUse(org.eclipse.swt.widgets.Shell, java.lang.Object)
     */
    public String[] getRemoteParentNamesInUse(Object element, IProgressMonitor monitor) throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public boolean validateDrop(Object src, Object target, boolean sameSystem)
    {
    	return true;	
    }

	/**
	 * This is a local RSE artifact so returning false
	 * 
	 * @param element the object to check
	 * @return false since this is not remote
	 */
	public boolean isRemote(Object element) {
		return false;
	}
}
