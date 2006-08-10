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

import samples.RSESamplesPlugin;
import samples.subsystems.DeveloperSubSystem;

/**
 * This is the adapter which enables us to work with our remote team resources.
 */
public class TeamResourceAdapter extends AbstractSystemViewAdapter implements
		ISystemRemoteElementAdapter {

	/**
	 * Constructor.
	 */
	public TeamResourceAdapter() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#addActions(org.eclipse.rse.ui.SystemMenuManager, org.eclipse.jface.viewers.IStructuredSelection, org.eclipse.swt.widgets.Shell, java.lang.String)
	 */
	public void addActions(SystemMenuManager menu,
			IStructuredSelection selection, Shell parent, String menuGroup)
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#getImageDescriptor(java.lang.Object)
	 */
	public ImageDescriptor getImageDescriptor(Object element)
	{
		return RSESamplesPlugin.getDefault().getImageDescriptor("ICON_ID_TEAM"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#getText(java.lang.Object)
	 */
	public String getText(Object element)
	{
		return ((TeamResource)element).getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#getAbsoluteName(java.lang.Object)
	 */
	public String getAbsoluteName(Object object)
	{
		TeamResource team = (TeamResource)object;
		return "Team_"+team.getName(); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#getType(java.lang.Object)
	 */
	public String getType(Object element)
	{
		return RSESamplesPlugin.getResourceString("property.team_resource.type"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#getParent(java.lang.Object)
	 */
	public Object getParent(Object element)
	{
		return null; // not really used, which is good because it is ambiguous
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element)
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object element)
	{
		return ((TeamResource)element).getDevelopers();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#internalGetPropertyDescriptors()
	 */
	protected IPropertyDescriptor[] internalGetPropertyDescriptors()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#internalGetPropertyValue(java.lang.Object)
	 */
	protected Object internalGetPropertyValue(Object key)
	{
		return null;
	}
	
	/**
	 * Intercept of parent method to indicate these objects
	 * can be renamed using the RSE-supplied rename action.
	 * 
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#canRename(java.lang.Object)
	 */
	public boolean canRename(Object element)
	{
		return true;
	}
	
	/**
	 * Intercept of parent method to actually do the rename. RSE supplies the rename GUI, but 
	 * defers the action work of renaming to this adapter method.
	 *  
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#doRename(Shell, Object, String)
	 */
	public boolean doRename(Shell shell, Object element, String newName)
	{
		((TeamResource)element).setName(newName);
		return true;
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
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getSubSystemConfigurationId(java.lang.Object)
	 */
	public String getSubSystemConfigurationId(Object element)
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
		return "team"; // Fine grained. Unique to this resource type. //$NON-NLS-1$
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
		TeamResource oldTeam = (TeamResource)oldElement;
		TeamResource newTeam = (TeamResource)newElement;
		newTeam.setName(oldTeam.getName());
		return false; // If developer objects held references to their team names, we'd have to return true
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteParent(org.eclipse.swt.widgets.Shell, java.lang.Object)
	 */
	public Object getRemoteParent(Shell shell, Object element) throws Exception
	{
		return null; // maybe this would be a Project or Roster object, or leave as null if this is the root 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteParentNamesInUse(org.eclipse.swt.widgets.Shell, java.lang.Object)
	 */
	public String[] getRemoteParentNamesInUse(Shell shell, Object element)
			throws Exception
	{
		DeveloperSubSystem ourSS = (DeveloperSubSystem)getSubSystem(element);
		TeamResource[] allTeams = ourSS.getAllTeams();
		String[] allNames = new String[allTeams.length];
		for (int idx=0; idx<allTeams.length; idx++)
		  allNames[idx] = allTeams[idx].getName();
		return allNames; // Return list of all team names 	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#supportsUserDefinedActions(java.lang.Object)
	 */
	public boolean supportsUserDefinedActions(Object object) {
		return false;
	}

}
