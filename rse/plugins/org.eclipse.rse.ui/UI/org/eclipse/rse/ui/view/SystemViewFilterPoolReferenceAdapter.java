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
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.SubSystemHelpers;
import org.eclipse.rse.core.subsystems.util.ISubSystemConfigurationAdapter;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.filters.ISystemFilterPoolReference;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ValidatorFilterPoolName;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;


/**
 * Adapter for displaying SystemFilterPool reference objects in tree views.
 * These are children of SubSystem objects
 */
public class SystemViewFilterPoolReferenceAdapter 
       extends AbstractSystemViewAdapter implements ISystemViewElementAdapter
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
		Object element = selection.getFirstElement();
		ISystemFilterPool pool = getFilterPool(element);
	    ISubSystemConfiguration ssFactory = getSubSystemConfiguration(pool);
	    ISubSystemConfigurationAdapter adapter = (ISubSystemConfigurationAdapter)ssFactory.getAdapter(ISubSystemConfigurationAdapter.class);
		
		IAction[] actions = adapter.getFilterPoolActions(ssFactory, pool, shell);
		if (actions != null)
		{
		  for (int idx=0; idx<actions.length; idx++)
		  {			
		  	 IAction action = actions[idx];		
		  	 menu.add(menuGroup, action);
		  }   
		}    	  
		actions = adapter.getFilterPoolReferenceActions(ssFactory, getFilterPoolReference(element), shell);
		if (actions != null)
		{
		  //menu.addSeparator();
		  for (int idx=0; idx<actions.length; idx++)
		  {
		  	 IAction action = actions[idx];		
		  	 menu.add(menuGroup, action);
		  }    
		}				
	}
	
	private ISubSystemConfiguration getSubSystemConfiguration(ISystemFilterPool pool)
	{
		return SubSystemHelpers.getParentSubSystemConfiguration(pool);
	}
	
	/**
     * <i>Overridden from parent.</i><br>
	 * Returns the subsystem that contains this object. 
	 */ 	
	public ISubSystem getSubSystem(Object element)
	{
		return ((ISubSystem)getFilterPoolReference(element).getProvider());
	}

	
	/**
	 * Returns an image descriptor for the image. More efficient than getting the image.
	 * @param element The element for which an image is desired
	 */
	public ImageDescriptor getImageDescriptor(Object element)
	{
    	ImageDescriptor poolImage = null;
    	ISystemFilterPool pool = getFilterPool(element);
    	if (pool.getProvider() != null)
    	{
    		ISubSystemConfigurationAdapter adapter = (ISubSystemConfigurationAdapter)pool.getProvider().getAdapter(ISubSystemConfigurationAdapter.class);
          poolImage = adapter.getSystemFilterPoolImage(pool); 
    	}
    	if (poolImage == null)
    	  poolImage = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_FILTERPOOL_ID);
    	return poolImage;  	
	}
	
	private ISystemFilterPoolReference getFilterPoolReference(Object element)
	{
		return (ISystemFilterPoolReference)element; // get referenced object
	}
	
	private ISystemFilterPool getFilterPool(Object element)
	{
		return getFilterPoolReference(element).getReferencedFilterPool(); // get master object
	}

	
	/**
	 * Return the label for this object. Uses getName() on the filter pool object.
	 */
	public String getText(Object element)
	{
		boolean qualifyNames = RSEUIPlugin.getTheSystemRegistry().getQualifiedHostNames();		
		if (!qualifyNames)
		  return getFilterPool(element).getName();
		else
		  return SubSystemHelpers.getParentSystemProfile(getFilterPool(element))+"." + getFilterPool(element).getName(); 
	}
	/**
	 * Return the name of this object, which may be different than the display text ({#link #getText(Object)}.
	 * <p>
	 * Called by common rename and delete actions.
	 */
	public String getName(Object element)
	{
		return getFilterPool(element).getName();
	}
	/**
	 * Return the absolute name, versus just display name, of this object
	 */
	public String getAbsoluteName(Object element)
	{
		ISystemFilterPoolReference filterPoolRef = (ISystemFilterPoolReference)element;
		return filterPoolRef.getReferencedFilterPool().getSystemFilterPoolManager().getName() + "." + filterPoolRef.getName();
	}			
	/**
	 * Return the type label for this object
	 */
	public String getType(Object element)
	{
		if (translatedType == null)
          translatedType = SystemViewResources.RESID_PROPERTY_FILTERPOOLREFERENCE_TYPE_VALUE;
		return translatedType;
	}	
	
	/**
	 * Return the parent of this object
	 */
	public Object getParent(Object element)
	{
		ISystemFilterPoolReference fpr = getFilterPoolReference(element);
		return SubSystemHelpers.getParentSubSystem(fpr);
	}	
	
	/**
	 * Return the children of this object.
	 * For filter pools, this is a list of filters.
	 */
	public Object[] getChildren(Object element)
	{
		ISystemFilterPoolReference fpRef = getFilterPoolReference(element);
		ISubSystem ss = getSubSystem(element);
		return fpRef.getSystemFilterReferences(ss);
	}
	
	/**
	 * Return true if this object has children
	 */
	public boolean hasChildren(Object element)
	{
		ISystemFilterPoolReference fpRef = getFilterPoolReference(element);
		return (fpRef.getReferencedFilterPool().getSystemFilterCount() > 0);
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
			propertyDescriptorArray[idx] = createSimplePropertyDescriptor(P_PARENT_FILTERPOOL, SystemViewResources.RESID_PROPERTY_FILTERPOOLREFERENCE_PARENTPOOL_LABEL, SystemViewResources.RESID_PROPERTY_FILTERPOOLREFERENCE_PARENTPOOL_TOOLTIP);

		  	// parent filter pool's profile
			propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(P_PROFILE, SystemViewResources.RESID_PROPERTY_FILTERPOOLREFERENCE_PARENTPROFILE_LABEL, SystemViewResources.RESID_PROPERTY_FILTERPOOLREFERENCE_PARENTPROFILE_TOOLTIP);

			// Related connection
			propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(P_RELATED_CONNECTION, SystemViewResources.RESID_PROPERTY_FILTERPOOLREFERENCE_RELATEDCONNECTION_LABEL, SystemViewResources.RESID_PROPERTY_FILTERPOOLREFERENCE_RELATEDCONNECTION_TOOLTIP);
		}		
		return propertyDescriptorArray;
	}
	/**
	 * Return our unique property values
	 */
	protected Object internalGetPropertyValue(Object key)
	{
		String name = (String)key;		
		//SystemFilterPoolReference ref = getFilterPoolReference(propertySourceInput);
		ISystemFilterPool pool = getFilterPool(propertySourceInput);
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
		ISystemFilterPool fp = getFilterPool(element);	
		return fp.isDeletable();
	}
	
	/**
	 * Perform the delete action.
	 * This physically deletes the filter pool and all references.
	 */
	public boolean doDelete(Shell shell, Object element, IProgressMonitor monitor) throws Exception
	{
		ISystemFilterPoolReference fpr = getFilterPoolReference(element);
		ISystemFilterPool fp = getFilterPool(element);
		ISystemFilterPoolManager fpMgr = fp.getSystemFilterPoolManager();
		fpMgr.deleteSystemFilterPool(fp);
		//SubSystemConfiguration ssParentFactory = getSubSystemConfiguration(fp); 			
		//ssParentFactory.deleteFilterPool(fp);
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
		ISystemFilterPool fp = getFilterPool(element);
		return !fp.isNonRenamable();
	}
	
	/**
	 * Perform the rename action. Assumes uniqueness checking was done already.
	 */
	public boolean doRename(Shell shell, Object element, String name) throws Exception
	{
		ISystemFilterPool fp = getFilterPool(element);
		ISystemFilterPoolManager fpMgr = fp.getSystemFilterPoolManager();
		fpMgr.renameSystemFilterPool(fp,name);
		//SubSystemConfiguration ssParentFactory = getSubSystemConfiguration(fp); 					
		//ssParentFactory.renameFilterPool(fp,name);
		return true;
	}	
	/**
	 * Return a validator for verifying the new name is correct.
	 */
    public ISystemValidator getNameValidator(Object element)
    {
		ISystemFilterPool fp = null;   
		if (element instanceof ISystemFilterPoolReference)
		  fp = getFilterPool(element);
		else
		  fp = (ISystemFilterPool)element;
		ISystemFilterPoolManager mgr = fp.getSystemFilterPoolManager();		  
		Vector v = mgr.getSystemFilterPoolNamesVector();
		/*
		if (fp != null) // might be called by the New wizard vs rename action
		  v.removeElement(fp.getName());
		*/
    	ISystemValidator nameValidator = new ValidatorFilterPoolName(v);
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
    	String mgrName = ((ISystemFilterPoolReference)element).getReferencedFilterPoolManagerName();
    	return (mgrName + "." + newName).toUpperCase();
    }

	// ------------------------------------------------------------
	// METHODS FOR SAVING AND RESTORING EXPANSION STATE OF VIEWER...
	// ------------------------------------------------------------

	/**
	 * Return what to save to disk to identify this element in the persisted list of expanded elements.
	 * This just defaults to getName, but if that is not sufficient override it here.
	 */
	public String getMementoHandle(Object element)
	{
		ISystemFilterPoolReference fpRef = (ISystemFilterPoolReference)element;
		return fpRef.getFullName();
	}
	/**
	 * Return what to save to disk to identify this element when it is the input object to a secondary
	 *  Remote Systems Explorer perspective.
	 */
	public String getInputMementoHandle(Object element)
	{
		Object parent = getParent(element);
		return getAdapter(parent).getInputMementoHandle(parent) + MEMENTO_DELIM + getMementoHandle(element);
	}
	/**
	 * Return a short string to uniquely identify the type of resource. Eg "conn" for connection.
	 * This just defaults to getType, but if that is not sufficient override it here, since that is
	 * a translated string.
	 */
	public String getMementoHandleKey(Object element)
	{
		return ISystemMementoConstants.MEMENTO_KEY_FILTERPOOLREFERENCE;
	}

}