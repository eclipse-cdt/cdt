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
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.texteditor.IUpdate;
 
/**
 * The superclass for all "Auto-Refresh" action delegates.
 */
public abstract class AbstractAutoRefreshActionDelegate extends ActionDelegate implements IViewActionDelegate, IUpdate {

	private IAction fAction;
	private IDebugView fView;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionDelegate#init(org.eclipse.jface.action.IAction)
	 */
	public void init( IAction action ) {
		setAction( action );
		super.init( action );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init( IViewPart view ) {
		setView( view );
		if ( getView() != null ) {
			getView().add( this );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		IAction action = getAction();
		if ( getView() != null && action != null ) {
			ICUpdateManager um = getUpdateManager( getView().getViewer().getInput() );
			action.setEnabled( ( um != null ) ? um.canUpdate() : false );
			action.setChecked( ( um != null ) ? um.getAutoModeEnabled() : false );
		}
	}

	public void run( IAction action ) {
		if ( getView() != null ) { 
			ICUpdateManager um = getUpdateManager( getView().getViewer().getInput() );
			if ( um != null )
				um.setAutoModeEnabled( action.isChecked() );
		}
	}

	protected IAction getAction() {
		return fAction;
	}

	private void setAction( IAction action ) {
		fAction = action;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionDelegate#dispose()
	 */
	public void dispose() {
		if ( getView() != null )
			getView().remove( this );
		super.dispose();
	}

	protected IDebugView getView() {
		return fView;
	}

	private void setView( IViewPart view ) {
		fView = ( view instanceof IDebugView ) ? (IDebugView)view : null;
	}

	protected abstract ICUpdateManager getUpdateManager( Object element );
}
