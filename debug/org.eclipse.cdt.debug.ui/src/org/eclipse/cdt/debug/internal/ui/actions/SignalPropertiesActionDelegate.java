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

import org.eclipse.cdt.debug.core.model.ICSignal;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionDelegate;

/**
 * Signal Properties action delegate.
 */
public class SignalPropertiesActionDelegate extends ActionDelegate implements IObjectActionDelegate {

	private ICSignal fSignal;
	private SignalPropertiesDialog fDialog;

	/**
	 * Constructor for SignalPropertiesActionDelegate.
	 * 
	 */
	public SignalPropertiesActionDelegate() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart( IAction action, IWorkbenchPart targetPart ) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged( IAction action, ISelection selection ) {
		if ( selection instanceof IStructuredSelection ) {
			Object element = ((IStructuredSelection)selection).getFirstElement();
			if ( element instanceof ICSignal ) {
				action.setEnabled( true );
				setSignal( (ICSignal)element );
				return;
			}
		}
		action.setEnabled( false );
		setSignal( null );
	}

	protected ICSignal getSignal() {
		return this.fSignal;
	}
	private void setSignal( ICSignal signal ) {
		this.fSignal = signal;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run( IAction action ) {
		if ( getSignal() != null ) {
			final MultiStatus ms = new MultiStatus( CDebugUIPlugin.getUniqueIdentifier(),
													DebugException.REQUEST_FAILED,
													ActionMessages.getString( "SignalPropertiesActionDelegate.Unable_to_change_signal_properties_1" ), //$NON-NLS-1$
													null );
			BusyIndicator.showWhile( Display.getCurrent(), 
									new Runnable() {
										public void run() {
											try {
												doAction( getSignal() );
											}
											catch( DebugException e ) {
												ms.merge( e.getStatus() );
											}
										}
									} );
			if ( !ms.isOK() ) {
				IWorkbenchWindow window = CDebugUIPlugin.getActiveWorkbenchWindow();
				if ( window != null ) {
					CDebugUIPlugin.errorDialog( ActionMessages.getString( "SignalPropertiesActionDelegate.Operation_failed_1" ), ms ); //$NON-NLS-1$
				}
				else {
					CDebugUIPlugin.log( ms );
				}
			}
		}
	}

	protected void doAction( ICSignal signal ) throws DebugException {
		IWorkbenchWindow window = CDebugUIPlugin.getActiveWorkbenchWindow();
		if ( window == null ) {
			return;
		}
		Shell activeShell = window.getShell();
		
		// If a previous edit is still in progress, don't start another
		if ( fDialog != null ) {
			return;
		}
		
		String name = signal.getName();
		String description = signal.getDescription();
		boolean pass = signal.isPassEnabled();
		boolean stop = signal.isStopEnabled();
		boolean canModify = signal.canModify();
		fDialog = new SignalPropertiesDialog( activeShell, name, description, pass, stop, canModify );
		try {
			if ( fDialog.open() == Window.OK ) {
				setProperties( signal, fDialog.isPassEnabled(), fDialog.isStopEnabled() );
			}
		}
		catch( DebugException e ) {
			throw e;
		}
		finally {
			fDialog = null;
		}
	}
	
	private void setProperties( ICSignal signal, boolean pass, boolean stop ) throws DebugException {
		signal.setPassEnabled( pass );
		signal.setStopEnabled( stop );
	}
}
