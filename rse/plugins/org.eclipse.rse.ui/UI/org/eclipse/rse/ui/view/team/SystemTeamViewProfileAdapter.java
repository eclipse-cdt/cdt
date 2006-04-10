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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.ISystemUserIdConstants;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.SystemResourceManager;
import org.eclipse.rse.model.ISystemProfile;
import org.eclipse.rse.model.SystemStartHere;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.actions.SystemCommonDeleteAction;
import org.eclipse.rse.ui.actions.SystemCommonRenameAction;
import org.eclipse.rse.ui.actions.SystemProfileNameCopyAction;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ValidatorProfileName;
import org.eclipse.rse.ui.view.AbstractSystemViewAdapter;
import org.eclipse.rse.ui.view.ISystemMementoConstants;
import org.eclipse.rse.ui.view.ISystemPropertyConstants;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.rse.ui.view.SystemViewResources;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.PropertyDescriptor;


/**
 * Adapter for displaying and processing SystemProfile objects in tree views, such as
 *  the Team view.
 */
public class SystemTeamViewProfileAdapter 
       extends AbstractSystemViewAdapter 
       implements ISystemViewElementAdapter, ISystemUserIdConstants
{
	
	private boolean actionsCreated = false;
	private Hashtable categoriesByProfile = new Hashtable();	
	// context menu actions for profiles...
	protected SystemTeamViewActiveProfileAction activeProfileAction;
	protected SystemTeamViewMakeActiveProfileAction makeProfileActiveAction;
	protected SystemTeamViewMakeInActiveProfileAction makeProfileInactiveAction;	
	protected SystemCommonRenameAction renameAction;	
	protected SystemCommonDeleteAction deleteAction;
	protected SystemProfileNameCopyAction copyProfileAction;
	
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

		boolean privateProfileStillExists = (SystemStartHere.getSystemProfileManager().getDefaultPrivateSystemProfile() != null);
		copyProfileAction.setProfile((ISystemProfile)selection.getFirstElement());
		
		if (activeProfileAction != null)
		{
			activeProfileAction.setEnabled(!privateProfileStillExists);
			menu.add(ISystemContextMenuConstants.GROUP_CHANGE,activeProfileAction);
		}
		else
		{
			menu.add(ISystemContextMenuConstants.GROUP_CHANGE,makeProfileActiveAction);
			menu.add(ISystemContextMenuConstants.GROUP_CHANGE,makeProfileInactiveAction);						
		}
		menu.add(ISystemContextMenuConstants.GROUP_REORGANIZE,copyProfileAction);
		menu.add(ISystemContextMenuConstants.GROUP_REORGANIZE,deleteAction);
		menu.add(ISystemContextMenuConstants.GROUP_REORGANIZE,renameAction);	    
	}
	private void createActions()
	{
		// activate profile action...
		// TODO: Delete the activeProfileAction logic when we have another mri rev, and can use the new actions.
		if (SystemResources.ACTION_PROFILE_MAKEACTIVE_LABEL.equals("test"))
			activeProfileAction = new SystemTeamViewActiveProfileAction(getShell());
		else
		{
			makeProfileActiveAction = new SystemTeamViewMakeActiveProfileAction(getShell());
			makeProfileInactiveAction = new SystemTeamViewMakeInActiveProfileAction(getShell());			 
		}

		copyProfileAction = new SystemProfileNameCopyAction(getShell());		

		deleteAction = new SystemCommonDeleteAction(getShell(),getTeamViewPart());
		deleteAction.setHelp(SystemPlugin.HELPPREFIX+"actndlpr");
		deleteAction.setDialogHelp(SystemPlugin.HELPPREFIX+"ddltprfl");
		deleteAction.setPromptLabel(SystemResources.RESID_DELETE_PROFILES_PROMPT);

		renameAction = new SystemCommonRenameAction(getShell(),getTeamViewPart());
		renameAction.setHelp(SystemPlugin.HELPPREFIX+"actnrnpr");
		renameAction.setDialogSingleSelectionHelp(SystemPlugin.HELPPREFIX+"drnsprfl");
		renameAction.setDialogMultiSelectionHelp(SystemPlugin.HELPPREFIX+"drnmprfl");
		renameAction.setSingleSelectPromptLabel(SystemResources.RESID_SIMPLE_RENAME_PROFILE_PROMPT_LABEL, 
												SystemResources.RESID_SIMPLE_RENAME_PROFILE_PROMPT_TIP);
		renameAction.setMultiSelectVerbage(SystemResources.RESID_MULTI_RENAME_PROFILE_VERBAGE);
				
		actionsCreated = true;		
	}
	
	/**
	 * Returns an image descriptor for the image. More efficient than getting the image.
	 * @param element The element for which an image is desired
	 */
	public ImageDescriptor getImageDescriptor(Object element)
	{
		ISystemProfile profile = (ISystemProfile)element;				
		if (SystemPlugin.getTheSystemRegistry().getSystemProfileManager().isSystemProfileActive(profile.getName()))
		  return SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_PROFILE_ACTIVE_ID);
		else
		  return SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_PROFILE_ID);
	}
	/**
	 * Return the team view part
	 */
	private SystemTeamViewPart getTeamViewPart()
	{
		SystemTeamView viewer = (SystemTeamView)getViewer();
		//System.out.println("Team view part set? " + (viewer != null));
		return viewer.getTeamViewPart();
	}
	
	/**
	 * Return the label for this object
	 */
	public String getText(Object element)
	{
		return ((ISystemProfile)element).getName();
	}

	/**
	 * Return the name of this object, which may be different than the display text ({#link #getText(Object)}.
	 * <p>
	 * Called by common rename and delete actions.
	 */
	public String getName(Object element)
	{
		return ((ISystemProfile)element).getName();
	}

	/**
	 * Return the absolute name, versus just display name, of this object
	 */
	public String getAbsoluteName(Object element)
	{
		return ((ISystemProfile)element).getName();
	}
		
	/**
	 * Return the type label for this object
	 */
	public String getType(Object element)
	{
		return SystemViewResources.RESID_PROPERTY_PROFILE_TYPE_VALUE;
	}	
	
	/**
	 * Return the string to display in the status line when the given object is selected.
	 * We return:
	 * Connection: name - Host name: hostName - Description: description
	 */
	public String getStatusLineText(Object element)
	{
		ISystemProfile profile = (ISystemProfile)element;
		boolean active = SystemPlugin.getTheSystemRegistry().getSystemProfileManager().isSystemProfileActive(profile.getName());
		return getType(element) + ": " + profile.getName() + ", " +
		       SystemViewResources.RESID_PROPERTY_PROFILESTATUS_LABEL + ": " +
		       (active ? SystemViewResources.RESID_PROPERTY_PROFILESTATUS_ACTIVE_LABEL : SystemViewResources.RESID_PROPERTY_PROFILESTATUS_NOTACTIVE_LABEL);
	}
			
	/**
	 * Return the parent of this object. We return the RemoteSystemsConnections project
	 */
	public Object getParent(Object element)
	{
		return SystemResourceManager.getRemoteSystemsProject();
	}
	
	/**
	 * Return the children of this profile. 
	 */
	public Object[] getChildren(Object element)
	{
		ISystemProfile profile = (ISystemProfile)element;	
		return getCategoryChildren(profile);
	}
	
	/**
	 * Given a profile, return all the category children for it. If this child objects have yet to be created,
	 *  create them now.
	 */
	public SystemTeamViewCategoryNode[] getCategoryChildren(ISystemProfile profile)
	{
		SystemTeamViewCategoryNode[] children = (SystemTeamViewCategoryNode[])categoriesByProfile.get(profile);
		if (children == null)
		{
			children = new SystemTeamViewCategoryNode[4]; //5];
			for (int idx=0; idx<children.length; idx++)
				children[idx] = new SystemTeamViewCategoryNode(profile);
			children[0].setLabel(SystemResources.RESID_TEAMVIEW_CATEGORY_CONNECTIONS_LABEL);
			children[0].setDescription(SystemResources.RESID_TEAMVIEW_CATEGORY_CONNECTIONS_TOOLTIP);
			children[0].setImageDescriptor(SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_CONNECTION_ID));
			children[0].setMementoHandle(SystemTeamViewCategoryNode.MEMENTO_CONNECTIONS);

			children[1].setLabel(SystemResources.RESID_TEAMVIEW_CATEGORY_FILTERPOOLS_LABEL);
			children[1].setDescription(SystemResources.RESID_TEAMVIEW_CATEGORY_FILTERPOOLS_TOOLTIP);
			children[1].setImageDescriptor(SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_FILTERPOOL_ID));
			children[1].setMementoHandle(SystemTeamViewCategoryNode.MEMENTO_FILTERPOOLS);

			children[2].setLabel(SystemResources.RESID_TEAMVIEW_CATEGORY_USERACTIONS_LABEL);
			children[2].setDescription(SystemResources.RESID_TEAMVIEW_CATEGORY_USERACTIONS_TOOLTIP);
			children[2].setImageDescriptor(SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_USERACTION_USR_ID));
			children[2].setMementoHandle(SystemTeamViewCategoryNode.MEMENTO_USERACTIONS);

			children[3].setLabel(SystemResources.RESID_TEAMVIEW_CATEGORY_COMPILECMDS_LABEL);
			children[3].setDescription(SystemResources.RESID_TEAMVIEW_CATEGORY_COMPILECMDS_TOOLTIP);
			children[3].setImageDescriptor(SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_COMPILE_ID));
			children[3].setMementoHandle(SystemTeamViewCategoryNode.MEMENTO_COMPILECMDS);
			
			/*
			children[4].setLabel(SystemResources.RESID_TEAMVIEW_CATEGORY_TARGETS_LABEL);
			children[4].setDescription(SystemResources.RESID_TEAMVIEW_CATEGORY_TARGETS_TOOLTIP);
			children[4].setImageDescriptor(SystemPlugin.getDefault().getImageDescriptor(ISystemConstants.ICON_SYSTEM_TARGET_ID));
			children[4].setMementoHandle(SystemTeamViewCategoryNode.MEMENTO_TARGETS);
			*/
						
			categoriesByProfile.put(profile, children);
		}
		return children;		
	}
	/**
	 * Given a profile and memento handle, return the appropriate category node. This is used when
	 *  restoring the expansion and selection state of the team view.
	 */
	public SystemTeamViewCategoryNode restoreCategory(ISystemProfile profile, String mementoHandle)
	{
		SystemTeamViewCategoryNode[] children = getCategoryChildren(profile);
		SystemTeamViewCategoryNode category = null;
		for (int idx=0; (category==null) && (idx<4); idx++)
		{
			if (children[idx].getMementoHandle().equals(mementoHandle))
				category = children[idx];
		}
		return category;
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
		  	propertyDescriptorArray = new PropertyDescriptor[1];
		 	SystemPlugin plugin = SystemPlugin.getDefault();
		 	int idx = 0;
		  	// status
			propertyDescriptorArray[idx++] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_IS_ACTIVE,SystemViewResources.RESID_PROPERTY_PROFILESTATUS_LABEL, SystemViewResources.RESID_PROPERTY_PROFILESTATUS_TOOLTIP);	      
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
		String name = (String)key;
		ISystemProfile profile = (ISystemProfile)propertySourceInput;		
		
		if (name.equals(P_IS_ACTIVE))
		{			
			boolean active = SystemPlugin.getTheSystemRegistry().getSystemProfileManager().isSystemProfileActive(profile.getName());
			if (active)
				return SystemViewResources.RESID_PROPERTY_PROFILESTATUS_ACTIVE_LABEL;
			else
				return SystemViewResources.RESID_PROPERTY_PROFILESTATUS_NOTACTIVE_LABEL;		  
		}
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
		boolean ok = true;
		if (!(element instanceof ISystemProfile))
		{
			ok = false;
			//System.out.println("selection: "+currObj.getClass().getName());
		}
		return ok;
	}	
	/**
	 * Perform the delete action.
	 */
	public boolean doDelete(Shell shell, Object element, IProgressMonitor monitor) throws Exception
	{
		boolean ok = true;
		SystemPlugin.getTheSystemRegistry().deleteSystemProfile((ISystemProfile)element);
		return ok;
	}
  
	// FOR COMMON RENAME ACTIONS	
	/**
	 * Return true if this object is renamable by the user. If so, when selected,
	 *  the Rename popup menu item will be enabled.
	 */
	public boolean canRename(Object element)
	{
		boolean ok = true;
		if (!(element instanceof ISystemProfile))
			ok = false;
		return ok;
	}	
	/**
	 * Perform the rename action.
	 */
	public boolean doRename(Shell shell, Object element, String newName) throws Exception
	{
		boolean ok = true;		
		SystemPlugin.getTheSystemRegistry().renameSystemProfile((ISystemProfile)element, newName);
		return ok;
	}    
	/**
	 * Return a validator for verifying the new name is correct.
	 */
    public ISystemValidator getNameValidator(Object element)
    {
		return new ValidatorProfileName(SystemPlugin.getTheSystemRegistry().getSystemProfileManager().getSystemProfileNamesVector());
    }
    /**
     * Parent override.
     * <p>
     * Form and return a new canonical (unique) name for this object, given a candidate for the new
     *  name. This is called by the generic multi-rename dialog to test that all new names are unique.
     *  To do this right, sometimes more than the raw name itself is required to do uniqueness checking.
     * <p>
     * Returns profile.connectionName, upperCased
     */
    public String getCanonicalNewName(Object element, String newName)
    {	
		return newName.toUpperCase(); 
    }

	// ------------------------------------------------------------
	// METHODS FOR SAVING AND RESTORING EXPANSION STATE OF VIEWER...
	// ------------------------------------------------------------
	/**
	 * Return what to save to disk to identify this element in the persisted list of expanded elements.
	 */
	public String getMementoHandle(Object element)
	{
		ISystemProfile profile = (ISystemProfile)element;	
		return profile.getName(); 
	}
	/**
	 * Return a short string to uniquely identify the type of resource. 
	 */
	public String getMementoHandleKey(Object element)
	{
		return ISystemMementoConstants.MEMENTO_KEY_PROFILE;
	}

}