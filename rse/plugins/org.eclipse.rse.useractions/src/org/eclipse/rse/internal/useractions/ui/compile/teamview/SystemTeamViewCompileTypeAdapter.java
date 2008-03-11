/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [182454] improve getAbsoluteName() documentation
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 *******************************************************************************/

package org.eclipse.rse.internal.useractions.ui.compile.teamview;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.internal.useractions.UserActionsResources;
import org.eclipse.rse.internal.useractions.ui.compile.SystemCompileCommand;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.view.AbstractSystemViewAdapter;
import org.eclipse.rse.ui.view.ISystemPropertyConstants;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.rse.useractions.ui.compile.SystemWorkWithCompileCommandsAction;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 * Adapter for displaying and processing SystemTeamViewCompileTypeNode objects in tree views, such as
 *  the Team view.
 */
public class SystemTeamViewCompileTypeAdapter extends AbstractSystemViewAdapter implements ISystemViewElementAdapter {
	private boolean actionsCreated = false;
	private SystemWorkWithCompileCommandsAction wwCmdsAction;
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
	public void addActions(SystemMenuManager menu, IStructuredSelection selection, Shell shell, String menuGroup) {
		if (!actionsCreated) createActions();
		wwCmdsAction.setShell(shell);
		menu.add(menuGroup, wwCmdsAction);
	}

	private void createActions() {
		actionsCreated = true;
		wwCmdsAction = new SystemWorkWithCompileCommandsAction(null, true);
	}

	/**
	 * Returns an image descriptor for the image. More efficient than getting the image.
	 * @param element The element for which an image is desired
	 */
	public ImageDescriptor getImageDescriptor(Object element) {
		return ((SystemTeamViewCompileTypeNode) element).getImageDescriptor();
	}

	/**
	 * Return the label for this object
	 */
	public String getText(Object element) {
		return ((SystemTeamViewCompileTypeNode) element).getLabel();
	}

	/**
	 * Return the name of this object, which may be different than the display text ({#link #getText(Object)}.
	 * <p>
	 * Called by common rename and delete actions.
	 */
	public String getName(Object element) {
		return ((SystemTeamViewCompileTypeNode) element).getLabel();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IRemoteObjectIdentifier#getAbsoluteName(java.lang.Object)
	 */
	public String getAbsoluteName(Object element) {
		SystemTeamViewCompileTypeNode type = (SystemTeamViewCompileTypeNode) element;
		ISystemProfile profile = type.getProfile();
		return profile.getName() + "." + type.getLabel(); //$NON-NLS-1$
	}

	/**
	 * Return the type label for this object
	 */
	public String getType(Object element) {
		return UserActionsResources.RESID_PROPERTY_TEAM_COMPILETYPE_TYPE_VALUE;
	}

	/**
	 * Return the string to display in the status line when the given object is selected.
	 */
	public String getStatusLineText(Object element) {
		SystemTeamViewCompileTypeNode type = (SystemTeamViewCompileTypeNode) element;
		return UserActionsResources.RESID_PROPERTY_TEAM_COMPILETYPE_TYPE_VALUE + ": " + type.getLabel(); //$NON-NLS-1$
	}

	/**
	 * Return the parent of this object. We return the RemoteSystemsConnections project
	 */
	public Object getParent(Object element) {
		SystemTeamViewCompileTypeNode type = (SystemTeamViewCompileTypeNode) element;
		return type.getParentSubSystemFactory();
	}

	/**
	 * Return the children of this profile. 
	 */
	public Object[] getChildren(IAdaptable element, IProgressMonitor mon) {
		SystemTeamViewCompileTypeNode type = (SystemTeamViewCompileTypeNode) element;
		SystemCompileCommand[] cmds = type.getCompileType().getCompileCommandsArray();
		SystemTeamViewCompileCommandNode[] nodes = null;
		if (cmds != null) {
			nodes = new SystemTeamViewCompileCommandNode[cmds.length];
			for (int idx = 0; idx < cmds.length; idx++)
				nodes[idx] = new SystemTeamViewCompileCommandNode(type, cmds[idx]);
		}
		return nodes;
	}

	/**
	 * Return true if this profile has children. We return true.
	 */
	public boolean hasChildren(IAdaptable element) {
		return true;
	}

	// Property sheet descriptors defining all the properties we expose in the Property Sheet
	/**
	 * Return our unique property descriptors, which getPropertyDescriptors adds to the common properties.
	 */
	protected org.eclipse.ui.views.properties.IPropertyDescriptor[] internalGetPropertyDescriptors() {
		if (propertyDescriptorArray == null) {
			propertyDescriptorArray = new PropertyDescriptor[1];
			int idx = 0;
			// file types
			propertyDescriptorArray[idx] = new PropertyDescriptor(ISystemPropertyConstants.P_COMPILETYPE_TYPES, UserActionsResources.RESID_PROPERTY_COMPILETYPE_TYPES_LABEL);
			propertyDescriptorArray[idx].setDescription(UserActionsResources.RESID_PROPERTY_COMPILETYPE_TYPES_DESCRIPTION);
			++idx;
		}
		return propertyDescriptorArray;
	}

	/**
	 * Returns the current value for the named property.
	 * The parent handles P_TEXT and P_TYPE only, and we augment that here. 
	 * @param	key - the name of the property as named by its property descriptor
	 * @return	the current value of the property
	 */
	public Object internalGetPropertyValue(Object key) {
		SystemTeamViewCompileTypeNode type = (SystemTeamViewCompileTypeNode) propertySourceInput;
		if (key.equals(ISystemPropertyConstants.P_COMPILETYPE_TYPES)) {
			return type.getCompileType().getType();
		} else
			return null;
	}
	// ------------------------------------------------------------
	// METHODS FOR SAVING AND RESTORING EXPANSION STATE OF VIEWER...
	// ------------------------------------------------------------
	//we currently don't support re-expanding past the profile level, for performance reasons:
	// we don't want to bring all subsystems to life to restore expansion state.
	/*
	 * Return what to save to disk to identify this element in the persisted list of expanded elements.
	 *
	 public String getMementoHandle(Object element)
	 {
	 SystemTeamViewCompileTypeNode type = (SystemTeamViewCompileTypeNode)element;	
	 return type.getMementoHandle(); 
	 }*/
	/*
	 * Return a short string to uniquely identify the type of resource. 
	 *
	 public String getMementoHandleKey(Object element)
	 {
	 SystemTeamViewCompileTypeNode type = (SystemTeamViewCompileTypeNode)element;	
	 return type.getProfile().getName() + "." + type.getParentCategory().getLabel() +"." + type.getParentSubSystemFactory().getName() + "." + type.getLabel(); 
	 }*/
	
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
