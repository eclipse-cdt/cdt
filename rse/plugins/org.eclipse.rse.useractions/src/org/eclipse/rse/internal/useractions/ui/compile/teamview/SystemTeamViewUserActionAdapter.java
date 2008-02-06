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
 * David McKnight   (IBM)        - [216252] [nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 *******************************************************************************/

package org.eclipse.rse.internal.useractions.ui.compile.teamview;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.internal.useractions.UserActionsIcon;
import org.eclipse.rse.internal.useractions.UserActionsResources;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDActionElement;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.view.AbstractSystemViewAdapter;
import org.eclipse.rse.ui.view.ISystemPropertyConstants;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 * Adapter for displaying and processing user action objects in tree views, such as
 *  the Team view.
 */
public class SystemTeamViewUserActionAdapter extends AbstractSystemViewAdapter implements ISystemViewElementAdapter {
	private boolean actionsCreated = false;
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
		//menu.add(menuGroup, copyAction);	    
	}

	private void createActions() {
		actionsCreated = true;
	}

	/**
	 * Returns an image descriptor for the image. More efficient than getting the image.
	 * @param element The element for which an image is desired
	 */
	public ImageDescriptor getImageDescriptor(Object element) {
		SystemUDActionElement action = (SystemUDActionElement) element;
		//return action.getImage();
		if (action.isIBM()) {
			if (action.isUserChanged())
				return UserActionsIcon.USERACTION_IBMUSR.getImageDescriptor();
			else
				return UserActionsIcon.USERACTION_IBM.getImageDescriptor();
		} else
			return UserActionsIcon.USERACTION_USR.getImageDescriptor();
	}

	/**
	 * Return the label for this object
	 */
	public String getText(Object element) {
		SystemUDActionElement action = (SystemUDActionElement) element;
		return action.getName(); // hmm, should it be getCommand()?
	}

	/**
	 * Return the name of this object, which may be different than the display text ({#link #getText(Object)}.
	 * <p>
	 * Called by common rename and delete actions.
	 */
	public String getName(Object element) {
		SystemUDActionElement action = (SystemUDActionElement) element;
		return action.getName();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IRemoteObjectIdentifier#getAbsoluteName(java.lang.Object)
	 */
	public String getAbsoluteName(Object element) {
		SystemUDActionElement action = (SystemUDActionElement) element;
		return action.getName();
	}

	/**
	 * Return the type label for this object
	 */
	public String getType(Object element) {
		return UserActionsResources.RESID_PROPERTY_TEAM_USERACTION_TYPE_VALUE;
	}

	/**
	 * Return the string to display in the status line when the given object is selected.
	 */
	public String getStatusLineText(Object element) {
		SystemUDActionElement action = (SystemUDActionElement) element;
		return UserActionsResources.RESID_TEAMVIEW_USERACTION_VALUE + ": " + action.getName(); //$NON-NLS-1$
	}

	/**
	 * Return the parent of this object. We return the RemoteSystemsConnections project
	 */
	public Object getParent(Object element) {
		SystemUDActionElement action = (SystemUDActionElement) element;
		return action.getData();
	}

	/**
	 * Return the children of this profile. 
	 */
	public Object[] getChildren(IAdaptable element, IProgressMonitor mon) {
		//SystemUDActionElement action = (SystemUDActionElement)element;
		return null;
	}

	/**
	 * Return true if this profile has children. We return false.
	 */
	public boolean hasChildren(IAdaptable element) {
		return false;
	}

	// Property sheet descriptors defining all the properties we expose in the Property Sheet
	/**
	 * Return our unique property descriptors, which getPropertyDescriptors adds to the common properties.
	 */
	protected IPropertyDescriptor[] internalGetPropertyDescriptors() {
		if (propertyDescriptorArray == null) {
			propertyDescriptorArray = new PropertyDescriptor[4];
			int idx = 0;
			// origin
			propertyDescriptorArray[idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_ORIGIN, UserActionsResources.RESID_PROPERTY_ORIGIN_LABEL,
					UserActionsResources.RESID_PROPERTY_ORIGIN_TOOLTIP);
			// command
			propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_COMMAND, UserActionsResources.RESID_PROPERTY_COMMAND_LABEL,
					UserActionsResources.RESID_PROPERTY_COMMAND_TOOLTIP);
			// comment
			propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_COMMENT, UserActionsResources.RESID_PROPERTY_COMMENT_LABEL,
					UserActionsResources.RESID_PROPERTY_COMMENT_TOOLTIP);
			// domain
			propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_USERACTION_DOMAIN, UserActionsResources.RESID_PROPERTY_USERACTION_DOMAIN_LABEL,
					UserActionsResources.RESID_PROPERTY_USERACTION_DOMAIN_TOOLTIP);
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
		String name = (String) key;
		SystemUDActionElement action = (SystemUDActionElement) propertySourceInput;
		//action.get
		if (name.equals(ISystemPropertyConstants.P_ORIGIN)) {
			if (action.isIBM()) {
				if (action.isUserChanged())
					return UserActionsResources.RESID_PROPERTY_ORIGIN_IBMUSER_VALUE;
				else
					return UserActionsResources.RESID_PROPERTY_ORIGIN_IBM_VALUE;
			} else
				return UserActionsResources.RESID_PROPERTY_ORIGIN_USER_VALUE;
		} else if (name.equals(ISystemPropertyConstants.P_COMMAND)) {
			return action.getCommand();
		} else if (name.equals(ISystemPropertyConstants.P_COMMENT)) {
			return action.getComment();
		} else if (name.equals(ISystemPropertyConstants.P_USERACTION_DOMAIN)) {
			int domain = action.getDomain();
			if (domain == -1)
				return UserActionsResources.RESID_PROPERTY_USERACTION_DOMAIN_ALL_VALUE;
			else
				return action.getManager().getActionSubSystem().getXlatedDomainNames()[domain];
		} else
			return null;
	}

	// ------------------------------------------------------------
	// METHODS FOR SAVING AND RESTORING EXPANSION STATE OF VIEWER...
	// ------------------------------------------------------------
	/**
	 * Return what to save to disk to identify this element in the persisted list of expanded elements.
	 */
	public String getMementoHandle(Object element) {
		return null; // not needed now as we don't re-expand to this level	 
	}

	/**
	 * Return a short string to uniquely identify the type of resource. 
	 */
	public String getMementoHandleKey(Object element) {
		return null; // not needed now as we don't re-expand to this level	  
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
