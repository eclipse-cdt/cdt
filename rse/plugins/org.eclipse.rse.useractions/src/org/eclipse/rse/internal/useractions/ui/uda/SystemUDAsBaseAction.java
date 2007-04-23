/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 *******************************************************************************/

package org.eclipse.rse.internal.useractions.ui.uda;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.events.ISystemResourceChangeListener;
import org.eclipse.rse.internal.useractions.UserActionsIcon;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.swt.widgets.Shell;

/**
 * Base action for user-defined actions, shown in the "User actions->" menu.
 * When one of these is run, it does variable substitution and runs the command.
 */
public class SystemUDAsBaseAction extends SystemBaseAction {
	private SystemUDActionSubsystem udaSubsystem;
	private SystemUDActionElement action;

	public SystemUDAsBaseAction(SystemUDActionElement action, Shell parent, SystemUDActionSubsystem udasubsys) {
		super(action.getLabel(), action.isIBM() ? UserActionsIcon.USERACTION_IBM.getImageDescriptor() : UserActionsIcon.USERACTION_USR.getImageDescriptor(), parent);
		this.udaSubsystem = udasubsys;
		this.action = action;
		allowOnMultipleSelection(true);
		// yantzi: artemis60, set SystemConnection to enable the offline support to
		// automatically disable UDA when the system connection is offline 
		setHost(udasubsys.getSubsystem().getHost());
	}

	/**
	 * This is the method called when the user selects this action.
	 * Child classes need to override this. If you need the parent shell,
	 * call getShell. If you need to know the current selection, call
	 * getSelection(), or getFirstSelection() followed by getNextSelection()
	 * until null is returned.
	 * @see Action#run()
	 */
	public void run() {
		IStructuredSelection selection = getSelection();
		if (viewer instanceof ISystemResourceChangeListener)
			udaSubsystem.run(getShell(), action, selection, (ISystemResourceChangeListener) viewer);
		else
			udaSubsystem.run(getShell(), action, selection, null);
	}
}
