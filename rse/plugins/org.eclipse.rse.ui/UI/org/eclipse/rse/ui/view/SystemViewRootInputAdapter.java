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

package org.eclipse.rse.ui.view;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.SystemPreferencesManager;
import org.eclipse.rse.internal.model.SystemNewConnectionPromptObject;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.swt.widgets.Shell;



/**
 * Adapter for the root-providing object of the SystemView tree viewer.
 */
public class SystemViewRootInputAdapter extends AbstractSystemViewAdapter implements ISystemViewElementAdapter
{
	private SystemPreferencesManager	spg;
    private SystemNewConnectionPromptObject newConnPrompt;
    private Object[] newConnPromptArray;
        
	/**
	 * Ctor
	 */
	public SystemViewRootInputAdapter()
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
		return SystemResources.RESID_SYSTEMREGISTRY_CONNECTIONS; 
	}
	/**
	 * Return the absolute name, versus just display name, of this object. 
	 * Just uses getText(element);
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
		//return "System Root Provider"; // should never be called
		// DKM - MRI hack to get "root"
		return SystemViewResources.RESID_PROPERTY_FILE_TYPE_ROOT_VALUE;	
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
	public Object[] getChildren(Object element)
	{
		ISystemViewInputProvider provider = (ISystemViewInputProvider)element;

        if ((provider instanceof ISystemRegistry) && showNewConnectionPrompt())
        {
          Object[] children = provider.getSystemViewRoots();
          if ((children == null) || (children.length == 0))
          {
            return getNewConnectionPromptObjectAsArray();
          }
          else
          {
          	Object[] allChildren = new Object[children.length+1];
          	allChildren[0] = getNewConnectionPromptObject();
          	for (int idx=0; idx<children.length; idx++)
          	   allChildren[idx+1] = children[idx];
          	return allChildren;
          }
        }		
		return provider.getSystemViewRoots();
	}
	
	/**
	 * Return true if this object has children
	 */
	public boolean hasChildren(Object element)
	{
		ISystemViewInputProvider provider = (ISystemViewInputProvider)element;
        if ((provider instanceof ISystemRegistry) && showNewConnectionPrompt())
        {
          return true;
        }
		return provider.hasSystemViewRoots();
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
    
    // PRIVATE METHODS...
    
    private boolean showNewConnectionPrompt()
    {
    	if (spg == null)
    	  spg = SystemPreferencesManager.getPreferencesManager();
    	return spg.getShowNewConnectionPrompt();
    }

    private SystemNewConnectionPromptObject getNewConnectionPromptObject()
    {
    	if (newConnPrompt == null)
    	  newConnPrompt = new SystemNewConnectionPromptObject();
    	return newConnPrompt;
    }

    private Object[] getNewConnectionPromptObjectAsArray()
    {
    	if (newConnPromptArray == null)
    	  newConnPromptArray = new Object[1];
    	newConnPromptArray[0] = getNewConnectionPromptObject();
    	return newConnPromptArray;
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


	/**
	 * Return what to save to disk to identify this element in the persisted list of expanded elements.
	 * We return "Connections"
	 */
	public String getMementoHandle(Object element)
	{
		return "Connections"; // this is what getName() returns, but if we xlate the name we want this to remain in english. 
	}
	/**
	 * Return a short string to uniquely identify the type of resource. Eg "conn" for connection.
	 * This just defaults to getType, but if that is not sufficient override it here, since that is
	 * a translated string.
	 */
	public String getMementoHandleKey(Object element)
	{
		// this really should not be translated... but changing it now might cause a problem restoring
		// from a previous release. Phil. 
		return getType(element);
		//return "root"; 
	}
}