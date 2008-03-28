/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
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
 * David Dykstal (IBM) - 180562: remove implementation of IRSEUserIdConstants
 * Martin Oberhuber (Wind River) - [182454] improve getAbsoluteName() documentation
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * David Dykstal (IBM) - [197036] rewrote getSubSystemConfigurationNodes to get filter pools
 *                                in a way that delays the loading of subsystem configurations
 * Xuan Chen     (IBM) - [223126] [api][breaking] Remove API related to User Actions in RSE Core/UI
 * Xuan Chen     (IBM) - [222263] Need to provide a PropertySet Adapter for System Team View
 *******************************************************************************/

package org.eclipse.rse.internal.ui.view.team;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.core.model.IPropertySetContainer;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.ISubSystemConfigurationProxy;
import org.eclipse.rse.core.subsystems.SubSystemConfiguration;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.internal.ui.view.SystemViewResources;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.view.AbstractSystemViewAdapter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.PropertyDescriptor;


/**
 * Adapter for displaying and processing SystemTeamViewCategoryNode objects in tree views, such as
 *  the Team view.
 */
public class SystemTeamViewCategoryAdapter 
       extends AbstractSystemViewAdapter {
	
	private boolean actionsCreated = false;
	//private Hashtable categoriesByProfile = new Hashtable();	
	
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IRemoteObjectIdentifier#getAbsoluteName(java.lang.Object)
	 */
	public String getAbsoluteName(Object element)
	{
		SystemTeamViewCategoryNode category = (SystemTeamViewCategoryNode)element;
		return category.getProfile() + "." + category.getLabel(); //$NON-NLS-1$
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
		return SystemResources.RESID_TEAMVIEW_CATEGORY_VALUE + ": " + category.getLabel() + " - " + category.getDescription(); //$NON-NLS-1$  //$NON-NLS-2$
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
	public Object[] getChildren(IAdaptable element, IProgressMonitor monitor)
	{
		SystemTeamViewCategoryNode category = (SystemTeamViewCategoryNode)element;	
		ISystemProfile profile = category.getProfile();
		String categoryType = category.getMementoHandle();
		if (categoryType.equals(SystemTeamViewCategoryNode.MEMENTO_CONNECTIONS))
		{
			return profile.getHosts();
		}
		else if (categoryType.equals(SystemTeamViewCategoryNode.MEMENTO_PROPERTYSETS))
		{
			return createSystemTeamViewPropertySetNodes(profile, category);
		}
		else
			return createSubSystemConfigurationNodes(profile, category);
	}
	
	
	/**
	 * Create subsystem configuration child nodes for expanded category node.
	 */
	private SystemTeamViewSubSystemConfigurationNode[] createSubSystemConfigurationNodes(ISystemProfile profile, SystemTeamViewCategoryNode category) {
		// create a sorted set to hold the subsystem configurations based on the proxy ordering
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
		final List proxies = Arrays.asList(sr.getSubSystemConfigurationProxies());
		Comparator comparator = new Comparator() {
			public int compare(Object o1, Object o2) {
				ISubSystemConfiguration c1 = (SubSystemConfiguration) o1;
				ISubSystemConfiguration c2 = (SubSystemConfiguration) o2;
				ISubSystemConfigurationProxy proxy1 = c1.getSubSystemConfigurationProxy();
				ISubSystemConfigurationProxy proxy2 = c2.getSubSystemConfigurationProxy();
				Integer p1 = new Integer(proxies.indexOf(proxy1));
				Integer p2 = new Integer(proxies.indexOf(proxy2));
				int result = p1.compareTo(p2);
				return result;
			}
		};
		SortedSet activeSubsystemConfigurations = new TreeSet(comparator);
		// find the active subsystem configurations
		for (Iterator z = proxies.iterator(); z.hasNext();) {
			ISubSystemConfigurationProxy proxy = (ISubSystemConfigurationProxy) z.next();
			if (proxy.isSubSystemConfigurationActive()) {
				ISubSystemConfiguration config = proxy.getSubSystemConfiguration();
				activeSubsystemConfigurations.add(config);
			}
		}
		// construct the nodes for the view based on these configurations
		List nodes = new ArrayList();
		String categoryType = category.getMementoHandle();
		for (Iterator z = activeSubsystemConfigurations.iterator(); z.hasNext();) {
			ISubSystemConfiguration ssf = (ISubSystemConfiguration) z.next();
			boolean createNode = false;
			if (categoryType.equals(SystemTeamViewCategoryNode.MEMENTO_FILTERPOOLS)) {
				createNode = ssf.supportsFilters() && (profile.getFilterPools(ssf).length > 0);
			} else if (categoryType.equals(SystemTeamViewCategoryNode.MEMENTO_TARGETS)) {
				createNode = ssf.supportsTargets(); // && profile.getTargets(ssf).length > 0;
			}
			if (createNode) {
				nodes.add(new SystemTeamViewSubSystemConfigurationNode(profile, category, ssf));
			}
		}
		SystemTeamViewSubSystemConfigurationNode[] result = new SystemTeamViewSubSystemConfigurationNode[nodes.size()];
		nodes.toArray(result);
		return result;
	}
	
	/**
	 * Create subsystem configuration child nodes for expanded category node.
	 */
	private SystemTeamViewPropertySetNode[] createSystemTeamViewPropertySetNodes(IPropertySetContainer profile, SystemTeamViewCategoryNode category) {
		
		IPropertySet[] propertySets = profile.getPropertySets();
		if (null == propertySets || propertySets.length == 0)
		{
			return new SystemTeamViewPropertySetNode[0];
		}
		// construct the nodes for the view based on these configurations
		List nodes = new ArrayList();
		//String categoryType = category.getMementoHandle();
		for (int i = 0; i < propertySets.length; i++) {
			nodes.add(new SystemTeamViewPropertySetNode(profile, propertySets[i]));
		}
		SystemTeamViewPropertySetNode[] result = new SystemTeamViewPropertySetNode[nodes.size()];
		nodes.toArray(result);
		return result;
	}
		
	/**
	 * Return true if this profile has children. We return true.
	 */
	public boolean hasChildren(IAdaptable element)
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
		 	RSEUIPlugin plugin = RSEUIPlugin.getDefault();
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
			boolean active = RSECorePlugin.getTheSystemRegistry().getSystemProfileManager().isSystemProfileActive(profile.getName());
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
		return category.getProfile().getName() + "." + category.getLabel(); //$NON-NLS-1$ 
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
