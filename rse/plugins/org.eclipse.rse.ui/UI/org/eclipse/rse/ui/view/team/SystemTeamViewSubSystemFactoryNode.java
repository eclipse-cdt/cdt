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
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.model.ISystemProfile;
import org.eclipse.rse.ui.SystemResources;


/**
 * This class represents a child node under category nodes, in the Team view.
 * It represents expandable subsystem factories such as "Files" or "iSeries Objects". 
 */
public class SystemTeamViewSubSystemFactoryNode implements IAdaptable
{
	private String mementoHandle;
	private ISystemProfile profile;
	private ISubSystemConfiguration ssf;
	private SystemTeamViewCategoryNode parentCategory;
	private String name = null;
	
	/**
	 * Constructor
	 */
	public SystemTeamViewSubSystemFactoryNode(ISystemProfile profile, SystemTeamViewCategoryNode parentCategory, ISubSystemConfiguration ssf)
	{
		super();
		this.profile = profile;
		this.ssf = ssf;
		this.parentCategory = parentCategory;
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
		if (o instanceof SystemTeamViewSubSystemFactoryNode)
		{
			SystemTeamViewSubSystemFactoryNode other = (SystemTeamViewSubSystemFactoryNode)o;
			if ((ssf == other.getSubSystemFactory()) &&
			 	(parentCategory == other.getParentCategory()) &&
			 	(profile == other.getProfile()))
			 	return true;
			else
				return false;
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
		return ssf.getImage();
	}

	/**
	 * Return this node's label
	 * @return the translated label to show in the tree, for this node
	 */
	public String getLabel()
	{
		if (name == null)
		{
			name = "";
			String[] types = ssf.getSystemTypes();
			if (ssf.getSubSystemFactoryProxy().supportsAllSystemTypes())
			{
				name = SystemResources.TERM_ALL;
			}
			else
			{
				for (int idx=0; idx<types.length; idx++)
				{
					if (idx==0)
						name += types[idx];
					else
						name += ", " + types[idx];
				}
			}
			name = ssf.getName() + " ("+name+")";
			//name = ssf.getName() + ": "+name;
		}
		return name;	
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
	 * Return the subsystem factory this node represents
	 */
	public ISubSystemConfiguration getSubSystemFactory()
	{
		return ssf;
	}

	/**
	 * Set the subsystem factory this node represents
	 */
	public void setSubSystemFactory(ISubSystemConfiguration factory)
	{
		ssf = factory;
	}

	/**
	 * Return the parent category this is a child of.
	 */
	public SystemTeamViewCategoryNode getParentCategory()
	{
		return parentCategory;
	}

	/**
	 * Set the parent category this is a child of.
	 */
	public void setParentCategory(SystemTeamViewCategoryNode node)
	{
		parentCategory = node;
	}

}