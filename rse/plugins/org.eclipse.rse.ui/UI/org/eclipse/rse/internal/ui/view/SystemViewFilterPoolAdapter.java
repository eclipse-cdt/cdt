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
 * Martin Oberhuber (Wind River) - [182454] improve getAbsoluteName() documentation
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [186748] Move ISubSystemConfigurationAdapter from UI/rse.core.subsystems.util
 * Xuan Chen        (IBM)        - [160775] [api] rename (at least within a zip) blocks UI thread
 * David Dykstal (IBM) - [226761] fix NPE in team view when expanding items
 *******************************************************************************/

package org.eclipse.rse.internal.ui.view;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolManager;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.SubSystemHelpers;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.subsystems.ISubSystemConfigurationAdapter;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ValidatorFilterPoolName;
import org.eclipse.rse.ui.view.AbstractSystemViewAdapter;
import org.eclipse.rse.ui.view.ISystemPropertyConstants;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;


/**
 * Adapter for displaying SystemFilterPool objects in tree views.
 * These are the masters, and only shown in work-with for the master.
 * These are children of SubSystemConfiguration objects
 */
public class SystemViewFilterPoolAdapter extends AbstractSystemViewAdapter
{
	protected String translatedType;
	//protected Object parent;
	
    // for reset property support
    //private String original_userId, original_port;
	// -------------------
	// property descriptors
	// -------------------
	private static PropertyDescriptor[] propertyDescriptorArray = null;	
	
	/**
	 * Returns any actions that should be contributed to the popup menu
	 * for the given subsystem object.
	 * Calls the method getActions on the subsystem's factory, and places
	 * all action objects returned from the call, into the menu.
	 * @param menu The menu to contribute actions to
	 * @param selection The window's current selection.
	 * @param shell Shell of viewer
	 * @param menuGroup recommended menu group to add actions to. If added to another group, you must be sure to create that group first.
	 */
	public void addActions(SystemMenuManager menu, IStructuredSelection selection, Shell shell, String menuGroup)
	{
		//if (selection.size() != 1)
		//  return; // does not make sense adding unique actions per multi-selection
		ISystemFilterPool pool = ((ISystemFilterPool)selection.getFirstElement());			
	    ISubSystemConfiguration ssFactory = SubSystemHelpers.getParentSubSystemConfiguration(pool);
	    ISubSystemConfigurationAdapter adapter = (ISubSystemConfigurationAdapter)ssFactory.getAdapter(ISubSystemConfigurationAdapter.class);
		IAction[] actions = adapter.getFilterPoolActions(menu, selection, shell, menuGroup, ssFactory, pool);
		if (actions != null)
		{
		  for (int idx=0; idx<actions.length; idx++)
		  {	
		  	 IAction action = actions[idx];		
		  	 menu.add(menuGroup, action);
		  }   
		}    	  
	}
	
	/**
	 * Returns an image descriptor for the image. More efficient than getting the image.
	 * @param element The element for which an image is desired
	 * @return the desired image descriptor
	 */
	public ImageDescriptor getImageDescriptor(Object element) {
		ImageDescriptor poolImage = null;
		ISystemFilterPool pool = (ISystemFilterPool) element;
		if (pool.getProvider() != null) {
			ISubSystemConfigurationAdapter adapter = (ISubSystemConfigurationAdapter) pool.getProvider().getAdapter(ISubSystemConfigurationAdapter.class);
			if (adapter != null) {
				poolImage = adapter.getSystemFilterPoolImage(pool);
			}
		}
		if (poolImage == null) {
			poolImage = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_FILTERPOOL_ID);
		}
		return poolImage;
	}
	
	/**
	 * Return the label for this object. Uses getName() on the filter pool object.
	 */
	public String getText(Object element)
	{
		return ((ISystemFilterPool)element).getName();
	}
	/**
	 * Return the name of this object, which may be different than the display text ({#link #getText(Object)}.
	 * <p>
	 * Called by common rename and delete actions.
	 */
	public String getName(Object element)
	{
		return ((ISystemFilterPool)element).getName();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IRemoteObjectIdentifier#getAbsoluteName(java.lang.Object)
	 */
	public String getAbsoluteName(Object element)
	{
		ISystemFilterPool filterPool = (ISystemFilterPool)element;
		return filterPool.getSystemFilterPoolManager().getName() + "." + filterPool.getName(); //$NON-NLS-1$
	}		
	/**
	 * Return the type label for this object
	 */
	public String getType(Object element)
	{
		if (translatedType == null)
          translatedType = SystemViewResources.RESID_PROPERTY_FILTERPOOL_TYPE_VALUE;
		return translatedType;
	}	
	
	/**
	 * Return the parent of this object. The parent of a filter pool is a subsystem factory,
	 *  in real life. But to a user, it is a subsystem.
	 */
	public Object getParent(Object element)
	{
		ISystemFilterPool fp = (ISystemFilterPool)element;
		// hmm, this will only work if a given factory only has one subsystem object...
		ISubSystemConfiguration ssParentFactory = SubSystemHelpers.getParentSubSystemConfiguration(fp);
		return ssParentFactory.getSubSystems(false)[0];				
	}	
	
	/**
	 * Return the children of this object.
	 * For filter pools, this is a list of filters.
	 */
	public Object[] getChildren(IAdaptable element, IProgressMonitor monitor)
	{
		ISystemFilterPool fp = (ISystemFilterPool)element;
		return fp.getSystemFilters();		
	}
	
	/**
	 * Return true if this object has children. That is, has filters.
	 */
	public boolean hasChildren(IAdaptable element)
	{
		ISystemFilterPool fp = (ISystemFilterPool)element;				
		return fp.getSystemFilterCount() > 0;
	}
	
    // Property sheet descriptors defining all the properties we expose in the Property Sheet
	/**
	 * Return our unique property descriptors
	 */
	protected IPropertyDescriptor[] internalGetPropertyDescriptors()
	{
		if (propertyDescriptorArray == null)
		{
			propertyDescriptorArray = new PropertyDescriptor[3];
			int idx = 0;

			// parent filter pool
			propertyDescriptorArray[idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_PARENT_FILTERPOOL, SystemViewResources.RESID_PROPERTY_FILTERPOOLREFERENCE_PARENTPOOL_LABEL, SystemViewResources.RESID_PROPERTY_FILTERPOOLREFERENCE_PARENTPOOL_TOOLTIP);

			// parent filter pool's profile
			propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_PROFILE, SystemViewResources.RESID_PROPERTY_FILTERPOOLREFERENCE_PARENTPROFILE_LABEL, SystemViewResources.RESID_PROPERTY_FILTERPOOLREFERENCE_PARENTPROFILE_TOOLTIP);

			// Related connection
			propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_RELATED_CONNECTION, SystemViewResources.RESID_PROPERTY_FILTERPOOLREFERENCE_RELATEDCONNECTION_LABEL, SystemViewResources.RESID_PROPERTY_FILTERPOOLREFERENCE_RELATEDCONNECTION_TOOLTIP);

		}		
		return propertyDescriptorArray;
	}
	/**
	 * Return our unique property values
	 */
	protected Object internalGetPropertyValue(Object key)
	{
		String name = (String)key;		
		ISystemFilterPool pool = (ISystemFilterPool)propertySourceInput;
		if (name.equals(ISystemPropertyConstants.P_PARENT_FILTERPOOL))
			return pool.getName();
		else if (name.equals(ISystemPropertyConstants.P_PROFILE))
			return pool.getSystemFilterPoolManager().getName();
		else if (name.equals(ISystemPropertyConstants.P_RELATED_CONNECTION))
			return (pool.getOwningParentName()==null) ? getTranslatedNotApplicable() : pool.getOwningParentName(); 
		else
			return null;
	}	
		
	// FOR COMMON DELETE ACTIONS
	/**
	 * Return true if this object is deletable by the user. If so, when selected,
	 *  the Edit->Delete menu item will be enabled.
	 */
	public boolean canDelete(Object element)
	{		
		ISystemFilterPool fp = (ISystemFilterPool)element;				
		return fp.isDeletable();
	}
	
	/**
	 * Perform the delete action.
	 */
	public boolean doDelete(Shell shell, Object element, IProgressMonitor monitor) throws Exception
	{
		ISystemFilterPool fp = (ISystemFilterPool)element;				
		ISystemFilterPoolManager fpMgr = fp.getSystemFilterPoolManager();
		fpMgr.deleteSystemFilterPool(fp);	
		return true;	
	}
	
	// FOR COMMON RENAME ACTIONS
	/**
	 * Return true if this object is renamable by the user. If so, when selected,
	 *  the Rename menu item will be enabled.
	 */
	public boolean canRename(Object element)
	{		
		if (!canDelete(element))
			return false;
		ISystemFilterPool fp = (ISystemFilterPool)element;
		return !fp.isNonRenamable();
	}
	
	/**
	 * Perform the rename action. Assumes uniqueness checking was done already.
	 */
	public boolean doRename(Shell shell, Object element, String name, IProgressMonitor monitor) throws Exception
	{
		ISystemFilterPool fp = (ISystemFilterPool)element;
		ISystemFilterPoolManager fpMgr = fp.getSystemFilterPoolManager();
		fpMgr.renameSystemFilterPool(fp,name);		
        return true;
	}	
	/**
	 * Return a validator for verifying the new name is correct.
	 */
	public ISystemValidator getNameValidator(Object element) {
		ISystemFilterPool fp = (ISystemFilterPool) element;
		ISystemFilterPoolManager mgr = fp.getSystemFilterPoolManager();
		String[] names = mgr.getSystemFilterPoolNames();
		ISystemValidator nameValidator = new ValidatorFilterPoolName(names);
		return nameValidator;
	}	

    /**
     * Parent override.
     * <p>
     * Form and return a new canonical (unique) name for this object, given a candidate for the new
     *  name. This is called by the generic multi-rename dialog to test that all new names are unique.
     *  To do this right, sometimes more than the raw name itself is required to do uniqueness checking.
     * <p>
     * Returns mgrName.poolName, upperCased
     */
    public String getCanonicalNewName(Object element, String newName)
    {
    	String mgrName = ((ISystemFilterPool)element).getSystemFilterPoolManager().getName();
    	return (mgrName + "." + newName).toUpperCase(); //$NON-NLS-1$
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
