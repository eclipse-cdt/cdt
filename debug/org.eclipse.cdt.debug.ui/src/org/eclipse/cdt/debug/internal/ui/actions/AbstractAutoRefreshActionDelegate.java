/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/ 
package org.eclipse.cdt.debug.internal.ui.actions; 

import org.eclipse.cdt.debug.core.ICUpdateManager;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * The superclass for all "Auto-Refresh" action delegates.
 */
public abstract class AbstractAutoRefreshActionDelegate extends AbstractRefreshActionDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractRefreshActionDelegate#doAction()
	 */
	protected void doAction() throws DebugException {
		IAction action = getAction();
		if ( action != null ) {
			IStructuredSelection selection = getSelection();
			if ( !selection.isEmpty() ) {
				ICUpdateManager um = getUpdateManager( selection.getFirstElement() );
				if ( um != null )
					um.setAutoModeEnabled( action.isChecked() );
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractRefreshActionDelegate#update()
	 */
	protected void update() {
		IAction action = getAction();
		if ( action != null ) {
			boolean enabled = false;
			boolean checked = false;
			IStructuredSelection selection = getSelection();
			if ( !selection.isEmpty() ) {
				ICUpdateManager um = getUpdateManager( selection.getFirstElement() );
				if ( um != null && um.canUpdate() )
					enabled = true;
				if ( um != null && um.getAutoModeEnabled() )
					checked = true;
			}
			action.setEnabled( enabled );
			action.setChecked( checked );
		}
	}
}
