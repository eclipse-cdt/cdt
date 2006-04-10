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
import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.ISystemUserIdConstants;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.model.ISystemProfile;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.view.AbstractSystemViewAdapter;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.rse.ui.view.SystemViewResources;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.PropertyDescriptor;


/**
 * Adapter for displaying and processing SystemTeamViewCategoryNode objects in tree views, such as
 *  the Team view.
 */
public class SystemTeamViewCategoryAdapter 
       extends AbstractSystemViewAdapter 
       implements ISystemViewElementAdapter, ISystemUserIdConstants
{
	
	private boolean actionsCreated = false;
	private Hashtable categoriesByProfile = new Hashtable();	
	
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
	    //menu.add(menuGroup, copyAction);	    
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
		SystemTeamViewCategoryNode category = (SystemTeamViewCategoryNode)element;				
	    return category.getImageDescriptor();
	}
	
	/**
	 * Return the label for this object
	 */
	public String getText(Object element)
	{
		SystemTeamViewCategoryNode category = (SystemTeamViewCategoryNode)element;
		return category.getLabel();
	}

	/**
	 * Return the name of this object, which may be different than the display text ({#link #getText(Object)}.
	 * <p>
	 * Called by common rename and delete actions.
	 */
	public String getName(Object element)
	{
		SystemTeamViewCategoryNode category = (SystemTeamViewCategoryNode)element;
		return category.getLabel();
	}

	/**
	 * Return the absolute name, versus just display name, of this object
	 */
	public String getAbsoluteName(Object element)
	{
		SystemTeamViewCategoryNode category = (SystemTeamViewCategoryNode)element;
		return category.getProfile() + "." + category.getLabel();
	}
		
	/**
	 * Return the type label for this object
	 */
	public String getType(Object element)
	{
		return SystemViewResources.RESID_PROPERTY_TEAM_CATEGORY_TYPE_VALUE;
	}	
	
	/**
	 * Return the string to display in the status line when the given object is selected.
	 * We return:
	 */
	public String getStatusLineText(Object element)
	{
		SystemTeamViewCategoryNode category = (SystemTeamViewCategoryNode)element;
		return SystemResources.RESID_TEAMVIEW_CATEGORY_VALUE + ": " + category.getLabel() + " - " + category.getDescription();
	}
			
	/**
	 * Return the parent of this object. We return the RemoteSystemsConnections project
	 */
	public Object getParent(Object element)
	{
		SystemTeamViewCategoryNode category = (SystemTeamViewCategoryNode)element;
		return category.getProfile();
	}
	
	/**
	 * Return the children of this profile. 
	 */
	public Object[] getChildren(Object element)
	{
		SystemTeamViewCategoryNode category = (SystemTeamViewCategoryNode)element;	
		ISystemProfile profile = category.getProfile();
		String categoryType = category.getMementoHandle();
		if (categoryType.equals(SystemTeamViewCategoryNode.MEMENTO_CONNECTIONS))
		{
			return profile.getHosts();
		}
		else
			return createSubSystemFactoryNodes(profile, category);
	}
	/**
	 * Create subsystem factory child nodes for expanded category node
	 */
	private SystemTeamViewSubSystemFactoryNode[] createSubSystemFactoryNodes(ISystemProfile profile, SystemTeamViewCategoryNode category)
	{
		SystemTeamViewSubSystemFactoryNode[] nodes = null;
		
		ISystemRegistry sr = SystemPlugin.getTheSystemRegistry();
		ISubSystemConfiguration[] factories = sr.getSubSystemConfigurations();
		if (factories != null)
		{
			Vector v = new Vector();
			String categoryType = category.getMementoHandle();
			for (int idx=0; idx<factories.length; idx++)
			{
				boolean createNode = false;
				ISubSystemConfiguration ssf = factories[idx];
				if (categoryType.equals(SystemTeamViewCategoryNode.MEMENTO_FILTERPOOLS))
				{
					createNode = ssf.supportsFilters() && (profile.getFilterPools(ssf).length > 0);
				}
				else if (categoryType.equals(SystemTeamViewCategoryNode.MEMENTO_USERACTIONS))
				{
					createNode = ssf.supportsUserDefinedActions(); // && profile.getUserActions(ssf).length > 0;
				}
				else if (categoryType.equals(SystemTeamViewCategoryNode.MEMENTO_COMPILECMDS))
				{
					createNode = ssf.supportsCompileActions(); // && profile.getCompileCommandTypes(ssf).length > 0;
				}
				else if (categoryType.equals(SystemTeamViewCategoryNode.MEMENTO_TARGETS))
				{
					createNode = ssf.supportsTargets(); // && profile.getTargets(ssf).length > 0;
				}
				if (createNode)
					v.addElement(new SystemTeamViewSubSystemFactoryNode(profile, category, factories[idx]));
			}
			nodes = new SystemTeamViewSubSystemFactoryNode[v.size()];
			for (int idx=0; idx<nodes.length; idx++)
			{
				nodes[idx] = (SystemTeamViewSubSystemFactoryNode)v.elementAt(idx);
			}
		}		
		return nodes;
	}
		
	/**
	 * Return true if this profile has children. We return true.
	 */
	public boolean hasChildren(Object element)
	{
		return true; 	
	}

    // Property sheet descriptors defining all the properties we expose in the Property Sheet
	/**
	 * Return our unique property descriptors, which getPropertyDescriptors adds to the common properties.
	 */
	protected org.eclipse.ui.views.properties.IPropertyDescriptor[] internalGetPropertyDescriptors()
	{
		if (propertyDescriptorArray == null)
		{
			/*
		  	propertyDescriptorArray = new PropertyDescriptor[1];
		 	SystemPlugin plugin = SystemPlugin.getDefault();
		 	int idx = 0;
		  	// status
		  	propertyDescriptorArray[idx] = new PropertyDescriptor(ISystemPropertyConstants.P_IS_ACTIVE, 
																SystemViewResources.RESID_PROPERTY_PROFILESTATUS_LABEL);
		  	propertyDescriptorArray[idx].setDescription(SystemViewResources.RESID_PROPERTY_PROFILESTATUS_DESCRIPTION));	      
		  	++idx;	
		  	*/      
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
		/*		
		if (name.equals(P_IS_ACTIVE))
		{			
			boolean active = SystemPlugin.getTheSystemRegistry().getSystemProfileManager().isSystemProfileActive(profile.getName());
			if (active)
				return SystemViewResources.RESID_PROPERTY_PROFILESTATUS_ACTIVE_LABEL);
			else
				return SystemViewResources.RESID_PROPERTY_PROFILESTATUS_NOTACTIVE_LABEL);		  
		}
		else
		*/		
		  return null;
	}	
	
    
	// ------------------------------------------------------------
	// METHODS FOR SAVING AND RESTORING EXPANSION STATE OF VIEWER...
	// ------------------------------------------------------------
	/**
	 * Return what to save to disk to identify this element in the persisted list of expanded elements.
	 */
	public String getMementoHandle(Object element)
	{
		SystemTeamViewCategoryNode category = (SystemTeamViewCategoryNode)element;
		return category.getMementoHandle();
	}
	/**
	 * Return a short string to uniquely identify the type of resource. 
	 */
	public String getMementoHandleKey(Object element)
	{
		SystemTeamViewCategoryNode category = (SystemTeamViewCategoryNode)element;	
		return category.getProfile().getName() + "." + category.getLabel(); 
	}

}