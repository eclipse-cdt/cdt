/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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
 * Martin Oberhuber (Wind River) - Adapted original tutorial code to Open RSE.
 ********************************************************************************/

package samples.model;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.view.AbstractSystemViewAdapter;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import samples.RSESamplesPlugin;

/**
 * This is the adapter which enables us to work with our remote developer resources.
 */
public class DeveloperResourceAdapter extends AbstractSystemViewAdapter
		implements ISystemRemoteElementAdapter
{

	/**
	 * Constructor 
	 */
	public DeveloperResourceAdapter() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#addActions(org.eclipse.rse.ui.SystemMenuManager, org.eclipse.jface.viewers.IStructuredSelection, org.eclipse.swt.widgets.Shell, java.lang.String)
	 */
	public void addActions(SystemMenuManager menu,
			IStructuredSelection selection, Shell parent, String menuGroup)
	{
	}

	/**
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(Object)
	 */
	public ImageDescriptor getImageDescriptor(Object object)
	{
		return RSESamplesPlugin.getDefault().getImageDescriptor("ICON_ID_DEVELOPER"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#getText(java.lang.Object)
	 */
	public String getText(Object element)
	{
		return ((DeveloperResource)element).getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#getAbsoluteName(java.lang.Object)
	 */
	public String getAbsoluteName(Object object)
	{
		DeveloperResource devr = (DeveloperResource)object;
		return "Devr_" + devr.getId(); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#getType(java.lang.Object)
	 */
	public String getType(Object element)
	{
		return RSESamplesPlugin.getResourceString("property.devr_resource.type"); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(Object)
	 */
	public Object getParent(Object o)
	{
		return null; // not really used, which is good because it is ambiguous
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element)
	{
		return false;
	}

	/**
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(Object)
	 */
	public Object[] getChildren(Object o)
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#internalGetPropertyDescriptors()
	 */
	protected IPropertyDescriptor[] internalGetPropertyDescriptors()
	{
		// the following array should be made static to it isn't created every time		
		PropertyDescriptor[] ourPDs = new PropertyDescriptor[2];
		ourPDs[0] = new PropertyDescriptor("devr_id", //$NON-NLS-1$
				RSESamplesPlugin.getResourceString("property.devr_id.name")); //$NON-NLS-1$
		ourPDs[0].setDescription(
				RSESamplesPlugin.getResourceString("property.devr_id.desc")); //$NON-NLS-1$
		ourPDs[1] = new PropertyDescriptor("devr_dept", //$NON-NLS-1$
				RSESamplesPlugin.getResourceString("property.devr_dept.name")); //$NON-NLS-1$
		ourPDs[1].setDescription(
				RSESamplesPlugin.getResourceString("property.devr_dept.desc")); //$NON-NLS-1$
		return ourPDs;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#internalGetPropertyValue(java.lang.Object)
	 */
	protected Object internalGetPropertyValue(Object key)
	{
		// propertySourceInput holds the currently selected object
		DeveloperResource devr = (DeveloperResource)propertySourceInput; 
		if (key.equals("devr_id")) //$NON-NLS-1$
			return devr.getId();
		else if (key.equals("devr_dept")) //$NON-NLS-1$
		  return devr.getDeptNbr();
		return null;
	}		
	// --------------------------------------
	// ISystemRemoteElementAdapter methods...
	// --------------------------------------

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getAbsoluteParentName(java.lang.Object)
	 */
	public String getAbsoluteParentName(Object element)
	{
		return "root"; // not really applicable as we have no unique hierarchy //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getSubSystemFactoryId(java.lang.Object)
	 */
	public String getSubSystemFactoryId(Object element)
	{
		return "samples.subsystems.factory"; // as declared in extension in plugin.xml //$NON-NLS-1$ 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteTypeCategory(java.lang.Object)
	 */
	public String getRemoteTypeCategory(Object element)
	{
		return "developers"; // Course grained. Same for all our remote resources. //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteType(java.lang.Object)
	 */
	public String getRemoteType(Object element)
	{
		return "developer"; // Fine grained. Unique to this resource type. //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteSubType(java.lang.Object)
	 */
	public String getRemoteSubType(Object element)
	{
		return null; // Very fine grained. We don't use it.
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#refreshRemoteObject(java.lang.Object, java.lang.Object)
	 */
	public boolean refreshRemoteObject(Object oldElement, Object newElement)
	{
		DeveloperResource oldDevr= (DeveloperResource)oldElement;
		DeveloperResource newDevr = (DeveloperResource)newElement;
		newDevr.setName(oldDevr.getName());
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteParent(org.eclipse.swt.widgets.Shell, java.lang.Object)
	 */
	public Object getRemoteParent(Shell shell, Object element) throws Exception
	{
		return null; // maybe this would be a Department obj, if we fully fleshed out our model
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteParentNamesInUse(org.eclipse.swt.widgets.Shell, java.lang.Object)
	 */
	public String[] getRemoteParentNamesInUse(Shell shell, Object element)
			throws Exception
	{
		// developers names do not have to be unique! So we don't need to implement this!
		return null;

	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#supportsUserDefinedActions(java.lang.Object)
	 */
	public boolean supportsUserDefinedActions(Object object) {
		return false;
	}

}
