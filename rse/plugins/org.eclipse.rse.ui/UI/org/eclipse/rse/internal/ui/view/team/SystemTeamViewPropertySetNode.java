/*********************************************************************************
 * Copyright (c) 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Xuan Chen (IBM) - [222263] initial contribution.
 *********************************************************************************/
 
package org.eclipse.rse.internal.ui.view.team;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.core.model.IProperty;
import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.core.model.IPropertySetContainer;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;


/**
 * This class represents a child node under category nodes, in the Team view.
 * It represents expandable subsystem factories such as "Files" or "iSeries Objects". 
 */
public class SystemTeamViewPropertySetNode implements IAdaptable
{
	private String mementoHandle;
	private IPropertySetContainer parent;
	private IPropertySet propertySet;
	static public final String NAME_PROPERTY = "name";  //$NON-NLS-1$
	static public final String NAME_PROPERTY1 = "Name";  //$NON-NLS-1$
	
	/**
	 * Constructor
	 */
	public SystemTeamViewPropertySetNode(IPropertySetContainer parent, IPropertySet propertySet)
	{
		super();
		this.parent = parent;
		this.propertySet = propertySet;
	}

	/**
	 * This is the method required by the IAdaptable interface.
	 * Given an adapter class type, return an object castable to the type, or
	 *  null if this is not possible.
	 */
	public Object getAdapter(Class adapterType)
	{
		return Platform.getAdapterManager().getAdapter(this, adapterType);	
	}           
	
	/**
	 * Compare this node to another. 
	 */
	public boolean equals(Object o)
	{
		if (o instanceof SystemTeamViewPropertySetNode)
		{
			/*
			SystemTeamViewPropertySetNode other = (SystemTeamViewPropertySetNode)o;
			if ((ssf == other.getSubSystemConfiguration()) &&
			 	(parentCategory == other.getParentCategory()) &&
			 	(profile == other.getProfile()))
			 	return true;
			else
				return false;
			*/
			return  super.equals(o);
		}
		else
			return super.equals(o);
	}
	
	/**
	 * Return this node's image
	 * @return the image to show in the tree, for this node
	 */
	public ImageDescriptor getImageDescriptor()
	{
		return RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_PROPERTIES_ID);
	}
	
	public Object getParent()
	{
		return parent;
	}

	/**
	 * Return this node's label
	 * @return the translated label to show in the tree, for this node
	 */
	public String getLabel()
	{
		String label = null;
		//First, check if this PropertySet has a property called "name"
		IProperty nameProperty = propertySet.getProperty(NAME_PROPERTY);
		if (null != nameProperty)
		{
			label = nameProperty.getValue();
		}
		else
		{
			label = propertySet.getName();
		}
		return label;
	}
	
	/**
	 * Convert to string. We call getLabel()
	 */
	public String toString()
	{
		return getLabel();
	}

	
	
	/**
	 * @return PropertySet this node is associated with
	 */
	public IPropertySet getPropertySet()
	{
		return propertySet;
	}

	/**
	 * @return the untranslated value to store in the memento, to uniquely identify this node
	 */
	public String getMementoHandle()
	{
		return mementoHandle;
	}

	/**
	 * Set the untranslated value to store in the memento, to uniquely identify this node
	 * @param string - untranslated value
	 */
	public void setMementoHandle(String string)
	{
		mementoHandle = string;
	}

}