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
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.subsystems.util.ISubsystemConfigurationAdapter;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.filters.ISystemFilterString;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;


/**
 * Default Adapter for displaying filter string objects in tree views.
 */
public class SystemViewFilterStringAdapter extends AbstractSystemViewAdapter implements ISystemViewElementAdapter, ISystemMessages
{
	//private static String translatedFilterString = null;	
	// -------------------
	// property descriptors
	// -------------------
	private static PropertyDescriptor[] propertyDescriptorArray = null;		
		
	/**
	 * Returns any actions that should be contributed to the popup menu
	 * for the given filter object.
	 * @param menu The menu to contribute actions to
	 * @param selection The window's current selection.
	 * @param shell Shell of viewer
	 * @param menuGroup recommended menu group to add actions to. If added to another group, you must be sure to create that group first.
	 */
	public void addActions(SystemMenuManager menu, IStructuredSelection selection, Shell shell, String menuGroup)
	{
		//if (selection.size() != 1)
		//  return; // does not make sense adding unique actions per multi-selection
		ISystemFilterString filterString = getFilterString(selection.getFirstElement());
		if (filterString.getParentSystemFilter().isTransient())
		  return;
		/*
	    SubSystemFactory ssFactory = SubSystemHelpers.getParentSubSystemFactory(filterString);
	    ssFactory.setConnection(null);
		IAction[] actions = ssFactory.getFilterActions(filter, shell);
		if (actions != null)
		{
		  for (int idx=0; idx<actions.length; idx++)
		  {
		  	 IAction action = actions[idx];		
		  	 menu.add(menuGroup, action);
		  }    
		}  */  	  		
	}

	private ISystemFilterString getFilterString(Object element)
	{
		return (ISystemFilterString)element;
	}
	
	/**
	 * Returns an image descriptor for the image. More efficient than getting the image.
	 * @param element The element for which an image is desired
	 */
	public ImageDescriptor getImageDescriptor(Object element)
	{
    	ImageDescriptor filterImage = null;
		ISystemFilterString filterString = getFilterString(element);
    	if (filterString.getProvider() != null)
    	{
    		ISubsystemConfigurationAdapter adapter = (ISubsystemConfigurationAdapter)filterString.getProvider().getAdapter(ISubsystemConfigurationAdapter.class);
          filterImage = adapter.getSystemFilterStringImage(filterString); 
    	}
    	if (filterImage == null)
    	  filterImage = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_FILTERSTRING_ID);
    	return filterImage;  	
	}
	
	/**
	 * Return the label for this object. Calls getName on the filter
	 */
	public String getText(Object element)
	{
		return getFilterString(element).getString();
	}
	/**
	 * Return the name of this object, which may be different than the display text ({#link #getText(Object)}.
	 * <p>
	 * Called by common rename and delete actions.
	 */
	public String getName(Object element)
	{
		return getFilterString(element).getString();
	}
	/**
	 * Return the absolute name, versus just display name, of this object
	 */
	public String getAbsoluteName(Object element)
	{
		ISystemFilterString filterString = getFilterString(element);
		ISystemFilter filter = filterString.getParentSystemFilter();
		return filter.getSystemFilterPoolManager().getName() + "." + filter.getParentFilterPool().getName() + "." + filter.getName() + "." + filterString.getString();
	}	
	/**
	 * Return the type label for this object
	 */
	public String getType(Object element)
	{
		//SystemFilterString filterString = getFilterString(element);
		//if (filterString.getParentSystemFilter().isTransient())
		  return SystemResources.RESID_PP_FILTERSTRING_TYPE_VALUE;
		//SubSystemFactory ssParentFactory = SubSystemHelpers.getParentSubSystemFactory(filterString.getParentSystemFilter());		
		//return ssParentFactory.getTranslatedFilterStringTypeProperty(filterString);
	}	
	
	/**
	 * Return the parent of this object. 
	 * This will be either a SubSystem object, or a filter object.
	 */
	public Object getParent(Object element)
	{
		ISystemFilterString filterString = getFilterString(element);
		return filterString.getParentSystemFilter();
	}
	
	/**
	 * Return the children of this filter.
	 * This returns an empty list.
	 */
	public Object[] getChildren(Object element)
	{
		return null;
	}
	
	
	/**
	 * Return true if this object has children. We return false;
	 */
	public boolean hasChildren(Object element)
	{
		return false;
	}

	/**
	 * Return our unique property descriptors
	 */
	protected IPropertyDescriptor[] internalGetPropertyDescriptors()
	{
		if (propertyDescriptorArray == null)
		{
			propertyDescriptorArray = new PropertyDescriptor[3];
			RSEUIPlugin plugin = RSEUIPlugin.getDefault();
			int idx = 0;
			// parent filter pool
			propertyDescriptorArray[idx] = createSimplePropertyDescriptor(P_PARENT_FILTERPOOL,SystemViewResources.RESID_PROPERTY_FILTERPARENTPOOL_LABEL, SystemViewResources.RESID_PROPERTY_FILTERPARENTPOOL_TOOLTIP);
			// parent filter
			propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(P_PARENT_FILTER,SystemViewResources.RESID_PROPERTY_FILTERPARENTFILTER_LABEL, SystemViewResources.RESID_PROPERTY_FILTERPARENTFILTER_TOOLTIP);	      
			// filter string
			propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(P_FILTERSTRING,SystemViewResources.RESID_PROPERTY_FILTERSTRING_LABEL, SystemViewResources.RESID_PROPERTY_FILTERSTRING_TOOLTIP);
		}		
		return propertyDescriptorArray;
	}
	/**
	 * Returns the current value for the named property.
	 * The parent handles P_TEXT and P_TYPE only, and we augment that here.
	 * @param	key the name of the property as named by its property descriptor
	 * @return	the current value of the property
	 */
	protected Object internalGetPropertyValue(Object key) 
	{
		String name = (String)key;		
	    ISystemFilterString filterString = getFilterString(propertySourceInput);

		if (name.equals(ISystemPropertyConstants.P_FILTERSTRING))
		{
	        return filterString.getString();
		}
		else if (name.equals(ISystemPropertyConstants.P_PARENT_FILTER))
		{
			return filterString.getParentSystemFilter().getName();
		}
		else if (name.equals(ISystemPropertyConstants.P_PARENT_FILTERPOOL))
		{
		  	if (filterString.getParentSystemFilter().isTransient())
		    	return getTranslatedNotApplicable();
		  	ISystemFilterPool parent = filterString.getParentSystemFilter().getParentFilterPool();
		  	if (parent != null)
		    	return parent.getName();
		  	else
		    	return null;
		}				
		else
 		  return null;
	}	
	
	// FOR COMMON DELETE ACTIONS		
	public boolean showDelete(Object element)
	{
		return !getFilterString(element).getParentSystemFilter().isTransient();
	}
	/**
	 * Return true if this object is deletable by the user. If so, when selected,
	 *  the Edit->Delete menu item will be enabled.
	 */
	public boolean canDelete(Object element)
	{
		return true;
	}
	
	/**
	 * Perform the delete action.
	 */
	public boolean doDelete(Shell shell, Object element, IProgressMonitor monitor)  throws Exception
	{
		ISystemFilterString filterString = getFilterString(element);
		ISystemFilter filter = filterString.getParentSystemFilter();	
		ISystemFilterPoolManager fpMgr = filterString.getSystemFilterPoolManager();
		Vector strings = filter.getSystemFiltersVector();
		String[] filterStrings = new String[strings.size()-1];
		for (int idx=0; idx<filterStrings.length; idx++)
			filterStrings[idx] = ((ISystemFilterString)strings.elementAt(idx)).toString();
		strings.remove(filterString);
		fpMgr.updateSystemFilter(filter, filter.getName(), filterStrings);		
		return true;
	}
	
	// FOR COMMON RENAME ACTIONS	
	public boolean showRename(Object element)
	{
		//return !getFilterString(element).getParentSystemFilter().isTransient();
		return false;
	}

	/**
	 * Return true if this object is renamable by the user. If so, when selected,
	 *  the Rename popup menu item will be enabled.
	 */
	public boolean canRename(Object element)
	{
		return false; //true;
	}	
	/**
	 * Perform the rename action.
	 */
	public boolean doRename(Shell shell, Object element, String name) throws Exception
	{
		return true;
	}    	

	// FOR COMMON REFRESH ACTIONS	
	public boolean showRefresh(Object element)
	{
		return false;
	}

	/**
	 * Return true if we should show the refresh action in the popup for the given element.
	 */
	public boolean showOpenViewActions(Object element)
	{
		return false;
	}
}