/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.ICUpdateManager;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * The superclass for all "Refresh" action delegates.
 */
public abstract class AbstractRefreshActionDelegate extends AbstractViewActionDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractViewActionDelegate#getErrorDialogTitle()
	 */
	protected String getErrorDialogTitle() {
		return ActionMessages.getString( "AbstractRefreshActionDelegate.Error_1" ); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractViewActionDelegate#getErrorDialogMessage()
	 */
	protected String getErrorDialogMessage() {
		return ActionMessages.getString( "AbstractRefreshActionDelegate.Error(s)_occurred_refreshing_the_view_1" ); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractViewActionDelegate#doAction()
	 */
	protected void doAction() throws DebugException {
		if ( getView() != null ) {
			ICUpdateManager um = getUpdateManager( getView().getViewer().getInput() );
			if ( um != null )
				um.update();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractViewActionDelegate#update()
	 */
	protected void update() {
		IAction action = getAction();
		if ( action != null ) {
			boolean enabled = false;
			IStructuredSelection selection = getSelection();
			if ( !selection.isEmpty() ) {
				ICUpdateManager um = getUpdateManager( selection.getFirstElement() );
				if ( um != null && um.canUpdate() )
					enabled = true;
			}
			action.setEnabled( enabled );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractViewActionDelegate#doHandleDebugEvent(org.eclipse.debug.core.DebugEvent)
	 */
	protected void doHandleDebugEvent( DebugEvent event ) {
		switch( event.getKind() ) {
			case DebugEvent.TERMINATE:
				update();
				break;
			case DebugEvent.RESUME:
				if ( !event.isEvaluation() || !((event.getDetail() & DebugEvent.EVALUATION_IMPLICIT) != 0) ) {
					update();
				}
				break;
			case DebugEvent.SUSPEND:
				update();
				break;
		}
	}

	protected abstract ICUpdateManager getUpdateManager( Object element );
}
