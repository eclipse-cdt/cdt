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
 * Tobias Schwarz (Wind River) - [181394] Include Context in getAbsoluteName() for filter and pool references
 * Martin Oberhuber (Wind River) - [182454] improve getAbsoluteName() documentation
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [186748] Move ISubSystemConfigurationAdapter from UI/rse.core.subsystems.util
 * Xuan Chen        (IBM)        - [160775] [api] rename (at least within a zip) blocks UI thread
 * Martin Oberhuber (Wind River) - [218304] Improve deferred adapter loading
 *******************************************************************************/

package org.eclipse.rse.internal.ui.view;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolManager;
import org.eclipse.rse.core.filters.ISystemFilterPoolManagerProvider;
import org.eclipse.rse.core.filters.ISystemFilterPoolReference;
import org.eclipse.rse.core.filters.ISystemFilterPoolReferenceManagerProvider;
import org.eclipse.rse.core.subsystems.ISubSystem;
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
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.rse.ui.view.SystemAdapterHelpers;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;


/**
 * Adapter for displaying SystemFilterPool reference objects in tree views.
 * These are children of SubSystem objects
 */
public class SystemViewFilterPoolReferenceAdapter
       extends AbstractSystemViewAdapter
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
	    if (adapter != null) {
			// Lazy Loading: By default, ISubSystemConfigurationAdapter will
			// only be available after its declaring bundle has been loaded,
			// which usually happens on "connect" of a subsystem. Before that
			// time, dynamically contributed actions will not be available.
			// Implementations that want their dynamic actions to be avaialble
			// earlier need to either declare them by static plugin.xml, or
			// provision for more eager loading of the bundle that declares
			// their adapter.
			IAction[] actions = adapter.getFilterPoolActions(menu, selection, shell, menuGroup, ssFactory, pool);
			if (actions != null) {
				for (int idx = 0; idx < actions.length; idx++) {
					IAction action = actions[idx];
					menu.add(menuGroup, action);
				}
			}
			actions = adapter.getFilterPoolReferenceActions(menu, selection, shell, menuGroup, ssFactory, (ISystemFilterPoolReference) element);
			if (actions != null) {
				// menu.addSeparator();
				for (int idx = 0; idx < actions.length; idx++) {
					IAction action = actions[idx];
					menu.add(menuGroup, action);
				}
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
		ISystemFilterPoolReference ref = (ISystemFilterPoolReference)element;
		return (ISubSystem)ref.getProvider();
	}

	/**
	 * Returns an image descriptor for the image. More efficient than getting the image.
	 * @param element The element for which an image is desired
	 */
	public ImageDescriptor getImageDescriptor(Object element) {
		ImageDescriptor poolImage = null;
		ISystemFilterPool pool = getFilterPool(element);
		if (pool != null) {
			ISystemFilterPoolManagerProvider provider = pool.getProvider();
			if (provider != null) {
				ISubSystemConfigurationAdapter adapter = (ISubSystemConfigurationAdapter) provider.getAdapter(ISubSystemConfigurationAdapter.class);
				if (adapter != null) {
					// Lazy Loading: Customized filter pool images will only be
					// available once the bundle that declares the
					// ISubSystemConfigurationAdapter has been activated. Until
					// that time, a default image is shown. Clients who want
					// their customized images be available earlier need to
					// provision for more eager loading of their bundles at the
					// right time (e.g. when expanding the SubSystem node,
					// rather than when connecting).
					poolImage = adapter.getSystemFilterPoolImage(pool);
				}
			}
		}
		if (poolImage == null) {
			poolImage = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_FILTERPOOL_ID);
		}
		return poolImage;
	}

	private ISystemFilterPool getFilterPool(Object element)
	{
		ISystemFilterPoolReference ref = (ISystemFilterPoolReference)element;
		ISystemFilterPool pool = ref.getReferencedFilterPool();
		return pool; // get master object
	}

	/**
	 * @param element the filter pool reference masquerading as an object
	 * @return the label for this filter pool reference.
	 */
	public String getText(Object element) {
		ISystemFilterPoolReference reference = (ISystemFilterPoolReference) element;
		String result = reference.getName();
		ISystemFilterPool pool = getFilterPool(element);
		if (pool != null) {
			result = pool.getName();
		}
		return result;
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IRemoteObjectIdentifier#getAbsoluteName(java.lang.Object)
	 */
	public String getAbsoluteName(Object element)
	{
		//TODO consider caching the absolute name in the FilterPoolReference to avoid unnecessary String operations - the name won't ever change
		ISystemFilterPoolReference filterPoolRef = (ISystemFilterPoolReference)element;
		ISystemFilterPoolReferenceManagerProvider subSystem = filterPoolRef.getProvider();
		ISystemViewElementAdapter adapter = SystemAdapterHelpers.getViewAdapter(subSystem);
		String parentAbsoluteName = (adapter != null) ?	adapter.getAbsoluteName(subSystem) : ""; //$NON-NLS-1$
		String referenceName = filterPoolRef.getName();
		String managerName = filterPoolRef.getReferencedFilterPoolManagerName();
		String absoluteName = parentAbsoluteName + "." + managerName + "." +  referenceName; //$NON-NLS-1$ //$NON-NLS-2$
		return absoluteName;
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
		ISystemFilterPoolReference fpr = (ISystemFilterPoolReference)element;
		return SubSystemHelpers.getParentSubSystem(fpr);
	}

	/**
	 * Return the children of this object.
	 * For filter pools, this is a list of filters.
	 */
	public Object[] getChildren(IAdaptable element, IProgressMonitor monitor)
	{
		ISystemFilterPoolReference fpRef = (ISystemFilterPoolReference)element;
		ISubSystem ss = getSubSystem(element);
		return fpRef.getSystemFilterReferences(ss);
	}

	/**
	 * Return true if this object has children
	 */
	public boolean hasChildren(IAdaptable element) {
		int count = 0;
		ISystemFilterPoolReference fpRef = (ISystemFilterPoolReference)element;
		if (fpRef != null) {
			ISystemFilterPool filterPool = fpRef.getReferencedFilterPool();
			if (filterPool != null) {
				count = filterPool.getSystemFilterCount();
			}
		}
		return count > 0;
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
	public boolean doRename(Shell shell, Object element, String name, IProgressMonitor monitor) throws Exception
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
	public ISystemValidator getNameValidator(Object element) {
		ISystemFilterPool fp = null;
		if (element instanceof ISystemFilterPoolReference) {
			fp = getFilterPool(element);
		} else if (element instanceof ISystemFilterPool) {
			fp = (ISystemFilterPool) element;
		} else {
			throw new IllegalArgumentException();
		}
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
    	String mgrName = ((ISystemFilterPoolReference)element).getReferencedFilterPoolManagerName();
    	return (mgrName + "." + newName).toUpperCase(); //$NON-NLS-1$
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
	 *  Remote System Explorer perspective.
	 */
	public String getInputMementoHandle(Object element)
	{
		Object parent = getParent(element);
		return SystemAdapterHelpers.getViewAdapter(parent, getViewer()).getInputMementoHandle(parent) + MEMENTO_DELIM + getMementoHandle(element);
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
