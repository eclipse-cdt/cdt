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

package org.eclipse.rse.ui.view.team;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.model.ISystemProfile;


/**
 * This class represents a child node under a profile, in the Team view.
 * It represents expandable categories such as "Connections", "Filter Pools", 
 * "User Actions" and "Compile Commands".
 */
public class SystemTeamViewCategoryNode implements IAdaptable
{
	private String label, mementoHandle, description;
	private ImageDescriptor imageDescriptor;
	private ISystemProfile profile;
	public static final String MEMENTO_CONNECTIONS = "conns"; 
	public static final String MEMENTO_FILTERPOOLS = "pools";
	public static final String MEMENTO_USERACTIONS = "actions";
	public static final String MEMENTO_COMPILECMDS = "cmds";
	public static final String MEMENTO_TARGETS = "targets";
		
	/**
	 * Constructor
	 */
	public SystemTeamViewCategoryNode(ISystemProfile profile)
	{
		super();
		this.profile = profile;
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
	 * Return this node's image
	 * @return the image to show in the tree, for this node
	 */
	public ImageDescriptor getImageDescriptor()
	{
		return imageDescriptor;
	}

	/**
	 * Return this node's label
	 * @return the translated label to show in the tree, for this node
	 */
	public String getLabel()
	{
		return label;
	}

	/**
	 * Set the image for this node
	 * @param descriptor ... the image to show in the tree, for this node
	 */
	public void setImageDescriptor(ImageDescriptor descriptor)
	{
		imageDescriptor = descriptor;
	}

	/**
	 * Set the label for this node
	 * @param string ... the label to show in the tree, for this node
	 */
	public void setLabel(String string)
	{
		label = string;
	}
	/**
	 * Convert to string. We call getLabel()
	 */
	public String toString()
	{
		return getLabel();
	}
	
	/**
	 * @return profile this category is associated with
	 */
	public ISystemProfile getProfile()
	{
		return profile;
	}

	/**
	 * @param profile ... the profile this category is associated with
	 */
	public void setProfile(ISystemProfile profile)
	{
		this.profile = profile;
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

	/**
	 * Return the description of this node. Shown on status line.
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * Set the description of this node. Shown on status line.
	 */
	public void setDescription(String string)
	{
		description = string;
	}

}