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
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.texteditor.IUpdate;
 
/**
 * The superclass for all "Refresh" action delegates.
 */
public abstract class AbstractRefreshActionDelegate extends ActionDelegate implements IViewActionDelegate, IUpdate {

	private IAction fAction;
	private IDebugView fView;

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
	 * @see org.eclipse.ui.actions.ActionDelegate#init(org.eclipse.jface.action.IAction)
	 */
	public void init( IAction action ) {
		setAction( action );
		super.init( action );
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

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		IAction action = getAction();
		if ( getView() != null && action != null ) {
			ICUpdateManager um = getUpdateManager( getView().getViewer().getInput() );
			action.setEnabled( ( um != null ) ? um.canUpdate() : false );
		}
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run( IAction action ) {
		final MultiStatus ms = new MultiStatus( CDebugUIPlugin.getUniqueIdentifier(), DebugException.REQUEST_FAILED, "", null ); //$NON-NLS-1$
		BusyIndicator.showWhile( Display.getCurrent(), new Runnable() {

			public void run() {
				try {
					doAction();
				}
				catch( DebugException e ) {
					ms.merge( e.getStatus() );
				}
			}
		} );
		if ( !ms.isOK() ) {
			IWorkbenchWindow window = DebugUIPlugin.getActiveWorkbenchWindow();
			if ( window != null ) {
				CDebugUIPlugin.errorDialog( CDebugUIPlugin.getResourceString( "internal.ui.actions.RefreshAction.Unable_to_refresh" ), ms.getChildren()[0] ); //$NON-NLS-1$
			}
			else {
				DebugUIPlugin.log( ms );
			}
		}		

	}

	protected IAction getAction() {
		return fAction;
	}

	private void setAction( IAction action ) {
		fAction = action;
	}

	protected void doAction() throws DebugException {
		if ( getView() != null ) { 
			ICUpdateManager um = getUpdateManager( getView().getViewer().getInput() );
			if ( um != null )
				um.update();
		}
	}

	protected abstract ICUpdateManager getUpdateManager( Object element );
}
