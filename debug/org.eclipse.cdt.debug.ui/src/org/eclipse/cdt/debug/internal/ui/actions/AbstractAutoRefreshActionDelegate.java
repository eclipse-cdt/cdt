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

import java.util.Observable;
import java.util.Observer;
import org.eclipse.cdt.debug.core.ICUpdateManager;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * The superclass for all "Auto-Refresh" action delegates.
 */
public abstract class AbstractAutoRefreshActionDelegate extends AbstractRefreshActionDelegate implements Observer{

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

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged( IWorkbenchPart part, ISelection selection ) {
		IStructuredSelection ss = getSelection();
		if ( !ss.isEmpty() ) {
			ICUpdateManager um = getUpdateManager( ss.getFirstElement() );
			if ( um instanceof Observable ) {
				((Observable)um).deleteObserver( this );
			}
		}
		super.selectionChanged( part, selection );
		ss = getSelection();
		if ( !ss.isEmpty() ) {
			ICUpdateManager um = getUpdateManager( ss.getFirstElement() );
			if ( um instanceof Observable ) {
				((Observable)um).addObserver( this );
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#dispose()
	 */
	public void dispose() {
		IStructuredSelection ss = getSelection();
		if ( !ss.isEmpty() ) {
			ICUpdateManager um = getUpdateManager( ss.getFirstElement() );
			if ( um instanceof Observable ) {
				((Observable)um).deleteObserver( this );
			}
		}
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update( Observable o, Object arg ) {
		update();
	}
}
