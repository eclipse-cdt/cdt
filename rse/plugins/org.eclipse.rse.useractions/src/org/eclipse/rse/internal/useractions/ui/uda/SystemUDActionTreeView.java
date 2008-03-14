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
 *******************************************************************************/
package org.eclipse.rse.internal.useractions.ui.uda;

import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.useractions.IUserActionsModelChangeEvents;
import org.eclipse.swt.widgets.Composite;

/**
 * In the Work With User Defined Actions dialog, this is the
 *  tree view for showing the existing actions.
 */
public class SystemUDActionTreeView extends SystemUDBaseTreeView {
	/**
	 * Constructor when we have a subsystem
	 */
	public SystemUDActionTreeView(Composite parent, ISystemUDWorkWithDialog editPane, ISubSystem ss, SystemUDActionSubsystem udaActionSubsystem) {
		super(parent, editPane, ss, udaActionSubsystem.getUDActionManager());
	}

	/**
	 * Constructor when we have a subsystem factory
	 */
	public SystemUDActionTreeView(Composite parent, ISystemUDWorkWithDialog editPane, ISubSystemConfiguration ssFactory, ISystemProfile profile) {
		super(parent, editPane, ssFactory, profile,
		// FIXME - UDA can't be coupled with subsystem API
				//((ISubsystemFactoryAdapter)ssFactory.getAdapter(ISubsystemFactoryAdapter.class)).getActionSubSystem(ssFactory, null).getUDActionManager()
				null);
	}

	/**
	 * Return the {@link org.eclipse.rse.core.events.ISystemModelChangeEvents} constant representing the resource type managed by this tree.
	 * This is a parent class override.
	 */
	protected int getResourceType() {
		return IUserActionsModelChangeEvents.SYSTEM_RESOURCETYPE_USERACTION;
	}

	/**
	 * Parent override.
	 * Restore the selected action to its IBM-supplied default value.
	 */
	public void doRestore() {
		SystemXMLElementWrapper selectedElement = getSelectedElement();
		if ((selectedElement == null) || !(selectedElement instanceof SystemUDActionElement)) return;
		SystemUDActionElement action = (SystemUDActionElement) selectedElement;
		boolean ok = getDocumentManager().getActionSubSystem().restoreDefaultAction(action, action.getDomain(), action.getOriginalName());
		if (ok) {
			action.setUserChanged(false);
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
	 * Select the given action
	 */
	public void selectAction(SystemUDActionElement element) {
		super.selectElement(element);
	}

	/**
	 * Refresh the parent of the given action.
	 * That is, find the parent and refresh the children.
	 * If the parent is not found, assume it is because it is new too,
	 *  so refresh the whole tree.
	 */
	public void refreshActionParent(SystemUDActionElement element) {
		super.refreshElementParent(element);
	}
}
