/********************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [186748] Move ISubSystemConfigurationAdapter from UI/rse.core.subsystems.util
 * Martin Oberhuber (Wind River) - [218304] Improve deferred adapter loading
 ********************************************************************************/

package org.eclipse.rse.internal.ui.view.team;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.ISubSystemConfigurationProxy;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.internal.ui.subsystems.SubSystemConfigurationProxyAdapter;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.subsystems.ISubSystemConfigurationAdapter;


/**
 * This class represents a child node under category nodes, in the Team view.
 * It represents expandable subsystem factories such as "Files" or "iSeries Objects".
 */
public class SystemTeamViewSubSystemConfigurationNode implements IAdaptable
{
	private String mementoHandle;
	private ISystemProfile profile;
	private ISubSystemConfiguration ssf;
	private SystemTeamViewCategoryNode parentCategory;
	private String name = null;

	/**
	 * Constructor
	 */
	public SystemTeamViewSubSystemConfigurationNode(ISystemProfile profile, SystemTeamViewCategoryNode parentCategory, ISubSystemConfiguration ssf)
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
		if (o instanceof SystemTeamViewSubSystemConfigurationNode)
		{
			SystemTeamViewSubSystemConfigurationNode other = (SystemTeamViewSubSystemConfigurationNode)o;
			if ((ssf == other.getSubSystemConfiguration()) &&
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
		ISubSystemConfigurationAdapter adapter = (ISubSystemConfigurationAdapter)ssf.getAdapter(ISubSystemConfigurationAdapter.class);
		if (adapter != null) {
			return adapter.getImage(ssf);
		} else {
			// Fall back to using the Proxy -- see also
			// SystemViewSubSystemAdapter.getImageDescriptor()
			ISubSystemConfigurationProxy proxy = ssf.getSubSystemConfigurationProxy();
			SubSystemConfigurationProxyAdapter proxyAdapter = (SubSystemConfigurationProxyAdapter) Platform.getAdapterManager().getAdapter(proxy,
					SubSystemConfigurationProxyAdapter.class);
			if (proxyAdapter != null) {
				return proxyAdapter.getImageDescriptor();
			} else {
				SystemBasePlugin.logWarning("Unexpected error: SubSystemConfiguration has no adapter and no proxyAdapter: " + ssf.getId()); //$NON-NLS-1$
				return null;
			}
		}
	}

	/**
	 * Return this node's label
	 * @return the translated label to show in the tree, for this node
	 */
	public String getLabel()
	{
		if (name == null)
		{
			StringBuffer buf = new StringBuffer();
			buf.append(ssf.getName());
			buf.append(" ("); //$NON-NLS-1$
			if (ssf.getSubSystemConfigurationProxy().supportsAllSystemTypes())
			{
				buf.append(SystemResources.TERM_ALL);
			}
			else
			{
				IRSESystemType[] types = ssf.getSystemTypes();
				for (int idx=0; idx<types.length; idx++)
				{
					if (idx>0) buf.append(", "); //$NON-NLS-1$
					buf.append(types[idx].getLabel());
				}
			}
			buf.append(")"); //$NON-NLS-1$
			name = buf.toString();
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
	public ISubSystemConfiguration getSubSystemConfiguration()
	{
		return ssf;
	}

	/**
	 * Set the subsystem factory this node represents
	 */
	public void setSubSystemConfiguration(ISubSystemConfiguration factory)
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