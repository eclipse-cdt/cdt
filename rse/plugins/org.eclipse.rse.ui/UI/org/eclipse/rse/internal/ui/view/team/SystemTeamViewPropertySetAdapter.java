/*********************************************************************************
 * Copyright (c) 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Xuan Chen (IBM) - [222263] initial contribution.
 * David McKnight   (IBM)        - [225506] [api][breaking] RSE UI leaks non-API type
 *********************************************************************************/

package org.eclipse.rse.internal.ui.view.team;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.model.IProperty;
import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.internal.ui.view.SystemViewResources;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.view.AbstractSystemViewAdapter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;


/**
 * Adapter for displaying and processing SystemTeamViewSubSystemConfigurationNode objects in tree views, such as
 *  the Team view.
 */
public class SystemTeamViewPropertySetAdapter 
       extends AbstractSystemViewAdapter
{
	private boolean actionsCreated = false;
	
	// -------------------
	// property descriptors
	// -------------------
	private static PropertyDescriptor[] propertyDescriptorArray = null;
	
	
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
		if (!actionsCreated)
		  createActions();	    
	}
	private void createActions()
	{
		actionsCreated = true;
	}
	
	/**
	 * Returns an image descriptor for the image. More efficient than getting the image.
	 * @param element The element for which an image is desired
	 */
	public ImageDescriptor getImageDescriptor(Object element)
	{				
	    return ((SystemTeamViewPropertySetNode)element).getImageDescriptor();
	}
	
	/**
	 * Return the label for this object
	 */
	public String getText(Object element)
	{
		return ((SystemTeamViewPropertySetNode)element).getLabel();
	}

	/**
	 * Return the name of this object, which may be different than the display text ({#link #getText(Object)}.
	 * <p>
	 * Called by common rename and delete actions.
	 */
	public String getName(Object element)
	{
		return ((SystemTeamViewPropertySetNode)element).getLabel();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IRemoteObjectIdentifier#getAbsoluteName(java.lang.Object)
	 */
	public String getAbsoluteName(Object element)
	{
		SystemTeamViewPropertySetNode factory = (SystemTeamViewPropertySetNode)element;
		return factory.getLabel(); 
	}
		
	/**
	 * Return the type label for this object
	 */
	public String getType(Object element)
	{
		return SystemViewResources.RESID_PROPERTY_TEAM_PROPERTYSET_TYPE_VALUE;
	}	
	
	/**
	 * Return the string to display in the status line when the given object is selected.
	 */
	public String getStatusLineText(Object element)
	{
		SystemTeamViewPropertySetNode factory = (SystemTeamViewPropertySetNode)element;
		return SystemResources.RESID_TEAMVIEW_PROPERTYSET_VALUE + ": " + factory.getLabel(); //$NON-NLS-1$
	}
			
	/**
	 * Return the parent of this object. We return the RemoteSystemsConnections project
	 */
	public Object getParent(Object element)
	{
		SystemTeamViewPropertySetNode factory = (SystemTeamViewPropertySetNode)element;
		return factory.getParent();
	}
	
	/**
	 * Return the children of this profile. 
	 */
	public Object[] getChildren(IAdaptable element, IProgressMonitor monitor)
	{		
		SystemTeamViewPropertySetNode propertySetNode = (SystemTeamViewPropertySetNode)element;
		IPropertySet propertySet = propertySetNode.getPropertySet();
		IPropertySet[] children = propertySet.getPropertySets();
		if (null == children || children.length == 0)
		{
			return new Object[0];
		}

		// construct the nodes for the view based on these configurations
		SystemTeamViewPropertySetNode[] nodes = new SystemTeamViewPropertySetNode[children.length];
		//String categoryType = category.getMementoHandle();
		for (int i = 0; i < children.length; i++)
		{
			nodes[i] = new SystemTeamViewPropertySetNode(propertySet, children[i]);
		}
		return nodes;
		
	}
		
	/**
	 * Return true if this profile has children. We return true.
	 */
	public boolean hasChildren(IAdaptable element)
	{
		SystemTeamViewPropertySetNode propertySetNode = (SystemTeamViewPropertySetNode)element;
		IPropertySet propertySet = propertySetNode.getPropertySet();
		IPropertySet[] children = propertySet.getPropertySets();
		if (null == children || children.length == 0)
		{
			return false;
		}
		
		return true;
		
	}
	
	/**
     * For PropertySet Node Adapter, we need to override this method since we don't want to display "name" and "type" twice.
	 * 
	 * @return an array containing all descriptors.  
	 * 
	 * @see #internalGetPropertyDescriptors()
	 */
	public IPropertyDescriptor[] getPropertyDescriptors() 
	{
		return internalGetPropertyDescriptors();
	}
	
	protected IPropertyDescriptor[] internalGetPropertyDescriptors()
	{
		SystemTeamViewPropertySetNode propertySetNode = (SystemTeamViewPropertySetNode)propertySourceInput;
		IPropertySet propertySet = propertySetNode.getPropertySet();
		
		String[] propertyKeys = propertySet.getPropertyKeys();
		
		int size = propertyKeys.length;
		
		IProperty nameProperty = propertySet.getProperty(SystemTeamViewPropertySetNode.NAME_PROPERTY);
		if (null == nameProperty)
		{
			nameProperty = propertySet.getProperty(SystemTeamViewPropertySetNode.NAME_PROPERTY1);
		}
		int startingIndex = 0;
		if (nameProperty == null)
		{
			size++;
		}
		
		propertyDescriptorArray = new PropertyDescriptor[size];
		
		if (nameProperty == null)
		{
			propertyDescriptorArray[startingIndex++] = createSimplePropertyDescriptor(SystemTeamViewPropertySetNode.NAME_PROPERTY, SystemTeamViewPropertySetNode.NAME_PROPERTY, propertySet.getName());
		}
		
		for (int i = startingIndex, j = 0; i < size; i++, j++)
		{
			propertyDescriptorArray[i] = createSimplePropertyDescriptor(propertyKeys[j], propertyKeys[j], propertySet.getPropertyValue(propertyKeys[j]));
		}
		  	
			
		return propertyDescriptorArray;
	}	
	
    
	/**
	 * Returns the current value for the named property.
	 * The parent handles P_TEXT and P_TYPE only, and we augment that here. 
	 * @param	key - the name of the property as named by its property descriptor
	 * @return	the current value of the property
	 */
	public Object internalGetPropertyValue(Object key)
	{
		SystemTeamViewPropertySetNode propertySetNode = (SystemTeamViewPropertySetNode)propertySourceInput;
		IPropertySet propertySet = propertySetNode.getPropertySet();
		return propertySet.getPropertyValue((String)key);
	}	
	
    
	// ------------------------------------------------------------
	// METHODS FOR SAVING AND RESTORING EXPANSION STATE OF VIEWER...
	// ------------------------------------------------------------
	/**
	 * Return what to save to disk to identify this element in the persisted list of expanded elements.
	 */
	public String getMementoHandle(Object element)
	{
		SystemTeamViewPropertySetNode factory = (SystemTeamViewPropertySetNode)element;	
		return factory.getMementoHandle(); 
	}
	/**
	 * Return a short string to uniquely identify the type of resource. 
	 */
	public String getMementoHandleKey(Object element)
	{
		SystemTeamViewPropertySetNode factory = (SystemTeamViewPropertySetNode)element;	
		return  factory.getLabel(); 
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