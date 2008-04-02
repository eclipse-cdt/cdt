/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * David Dykstal (IBM) - [186589] move user types, user actions, and compile commands
 *                                API to the user actions plugin
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 * Kevin Doyle		(IBM)		 - [222831] Can't Delete User Actions/Named Types                               
 *******************************************************************************/
package org.eclipse.rse.internal.useractions.ui.uda;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.useractions.Activator;
import org.eclipse.rse.internal.useractions.IUserActionsMessageIds;
import org.eclipse.rse.internal.useractions.IUserActionsModelChangeEvents;
import org.eclipse.rse.internal.useractions.UserActionsResources;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.swt.widgets.Composite;

/**
 * In the Work With User Defined File Types dialog, this is the
 *  tree view for showing the existing types.
 */
public class SystemUDTypeTreeView extends SystemUDBaseTreeView {
	/**
	 * Constructor when we have a subsystem
	 */
	public SystemUDTypeTreeView(Composite parent, ISystemUDWorkWithDialog editPane, ISubSystem ss, SystemUDActionSubsystem udaActionSubsystem) {
		/* FIXME - UDA not coupled with subsystem API anymore */
		super(parent, editPane, ss, udaActionSubsystem.getUDTypeManager());
	}

	/**
	 * Constructor when we have a subsystem factory and profile
	 */
	public SystemUDTypeTreeView(Composite parent, ISystemUDWorkWithDialog editPane, ISubSystemConfiguration ssFactory, ISystemProfile profile) {
		super(parent, editPane, ssFactory, profile,
		/* FIXME - UDA not coupled with subsystem API anymore
		 ((ISubsystemFactoryAdapter)ssFactory.getAdapter(ISubsystemFactoryAdapter.class)).getActionSubSystem(ssFactory, null).getUDTypeManager()
		 */
		null);
	}

	/**
	 * Return types manager
	 */
	public SystemUDTypeManager getTypeManager() {
		return (SystemUDTypeManager) super.getDocumentManager();
	}

	/**
	 * Get the selected type name.
	 * Returns "" if nothing selected
	 */
	public String getSelectedTypeName() {
		return super.getSelectedElementName();
	}

	/**
	 * Get the selected type domain.
	 * Returns -1 if nothing selected or domains not supported
	 */
	public int getSelectedTypeDomain() {
		return super.getSelectedElementDomain();
	}

	/**
	 * Return message for delete confirmation
	 */
	protected SystemMessage getDeleteConfirmationMessage() {
		SystemMessage msg = new SimpleSystemMessage(Activator.PLUGIN_ID, 
				IUserActionsMessageIds.MSG_CONFIRM_DELETE_USERTYPE,
				IStatus.WARNING, UserActionsResources.MSG_CONFIRM_DELETE_USERTYPE, UserActionsResources.MSG_CONFIRM_DELETE_USERTYPE_DETAILS);
		msg.setIndicator(SystemMessage.INQUIRY);
		return msg;
	}

	/**
	 * Return the {@link org.eclipse.rse.core.events.ISystemModelChangeEvents} constant representing the resource type managed by this tree.
	 * This is a parent class override.
	 */
	protected int getResourceType() {
		return IUserActionsModelChangeEvents.SYSTEM_RESOURCETYPE_NAMEDTYPE;
	}

	/**
	 * Parent override.
	 * Restore the selected type to its IBM-supplied default value.
	 */
	public void doRestore() {
		SystemXMLElementWrapper selectedElement = getSelectedElement();
		if ((selectedElement == null) || !(selectedElement instanceof SystemUDTypeElement)) return;
		SystemUDTypeElement type = (SystemUDTypeElement) selectedElement;
		boolean ok = getDocumentManager().getActionSubSystem().restoreDefaultType(type, type.getDomain(), type.getOriginalName());
		if (ok) {
			type.setUserChanged(false);
			getDocumentManager().saveUserData(profile);
			selectElement(selectedElement);
			String[] allProps = { IBasicPropertyConstants.P_TEXT, IBasicPropertyConstants.P_IMAGE };
			update(selectedElement, allProps);
		}
	}

	// ------------------------------------
	// HELPER METHODS CALLED FROM EDIT PANE
	// ------------------------------------
	/**
	 * Select the given type
	 */
	public void selectType(SystemUDTypeElement element) {
		super.selectElement(element);
	}

	/**
	 * Refresh the parent of the given action.
	 * That is, find the parent and refresh the children.
	 * If the parent is not found, assume it is because it is new too,
	 *  so refresh the whole tree.
	 */
	public void refreshTypeParent(SystemUDTypeElement element) {
		super.refreshElementParent(element);
	}
}
