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
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionDelegate;

/**
 * The superclass for all "Refresh" action delegates.
 */
public abstract class AbstractRefreshActionDelegate extends ActionDelegate implements IViewActionDelegate, ISelectionListener, INullSelectionListener, IDebugEventSetListener {

	private IAction fAction;

	private IDebugView fView;

	private IStructuredSelection fSelection;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init( IViewPart view ) {
		setView( view );
		DebugPlugin.getDefault().addDebugEventListener( this );
		IWorkbenchWindow window = getWindow();
		if ( window != null ) {
			window.getSelectionService().addSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, this );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.ActionDelegate#init(org.eclipse.jface.action.IAction)
	 */
	public void init( IAction action ) {
		setAction( action );
		action.setEnabled( false );
		super.init( action );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.ActionDelegate#dispose()
	 */
	public void dispose() {
		IWorkbenchWindow window = getWindow();
		if ( window != null ) {
			window.getSelectionService().removeSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, this );
		}
		DebugPlugin.getDefault().removeDebugEventListener( this );
		super.dispose();
	}

	protected IDebugView getView() {
		return fView;
	}

	private void setView( IViewPart view ) {
		fView = (view instanceof IDebugView) ? (IDebugView)view : null;
	}

	/*
	 * (non-Javadoc)
	 * 
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged( IWorkbenchPart part, ISelection selection ) {
		setSelection( selection );
		update();
	}

	private IWorkbenchWindow getWindow() {
		if ( getView() != null ) {
			return getView().getViewSite().getWorkbenchWindow();
		}
		return null;
	}

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

	protected abstract ICUpdateManager getUpdateManager( Object element );

	protected IStructuredSelection getSelection() {
		return fSelection;
	}

	private void setSelection( ISelection selection ) {
		fSelection = (selection instanceof IStructuredSelection) ? (IStructuredSelection)selection : null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
	 */
	public void handleDebugEvents( final DebugEvent[] events ) {
		if ( getWindow() == null || getAction() == null ) {
			return;
		}
		Shell shell = getWindow().getShell();
		if ( shell == null || shell.isDisposed() ) {
			return;
		}
		Runnable r = new Runnable() {

			public void run() {
				for( int i = 0; i < events.length; i++ ) {
					if ( events[i].getSource() != null ) {
						doHandleDebugEvent( events[i] );
					}
				}
			}
		};
		shell.getDisplay().asyncExec( r );
	}

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
}