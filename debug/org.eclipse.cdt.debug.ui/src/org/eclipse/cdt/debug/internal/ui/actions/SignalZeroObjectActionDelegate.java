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

import org.eclipse.cdt.debug.core.model.IResumeWithoutSignal;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionDelegate;

/**
 * The object contribution delegate of the "Resume Without Signal" action.
 */
public class SignalZeroObjectActionDelegate extends ActionDelegate implements IObjectActionDelegate {

	private IResumeWithoutSignal fTarget = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart( IAction action, IWorkbenchPart targetPart ) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	public void run( IAction action ) {
		if ( getTarget() != null ) {
			final MultiStatus ms = new MultiStatus( CDebugUIPlugin.getUniqueIdentifier(), DebugException.REQUEST_FAILED, ActionMessages.getString( "SignalZeroObjectActionDelegate.0" ), null ); //$NON-NLS-1$
			BusyIndicator.showWhile( Display.getCurrent(), new Runnable() {

				public void run() {
					try {
						doAction( getTarget() );
					}
					catch( DebugException e ) {
						ms.merge( e.getStatus() );
					}
				}
			} );
			if ( !ms.isOK() ) {
				IWorkbenchWindow window = CDebugUIPlugin.getActiveWorkbenchWindow();
				if ( window != null ) {
					CDebugUIPlugin.errorDialog( ActionMessages.getString( "SignalZeroObjectActionDelegate.1" ), ms ); //$NON-NLS-1$
				}
				else {
					CDebugUIPlugin.log( ms );
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged( IAction action, ISelection selection ) {
		if ( selection instanceof IStructuredSelection ) {
			Object element = ((IStructuredSelection)selection).getFirstElement();
			if ( element instanceof IResumeWithoutSignal ) {
				boolean enabled = ((IResumeWithoutSignal)element).canResumeWithoutSignal();
				action.setEnabled( enabled );
				if ( enabled ) {
					setTarget( (IResumeWithoutSignal)element );
					return;
				}
			}
		}
		action.setEnabled( false );
		setTarget( null );
	}

	protected void doAction( IResumeWithoutSignal target ) throws DebugException {
		target.resumeWithoutSignal();
	}

	protected IResumeWithoutSignal getTarget() {
		return fTarget;
	}

	protected void setTarget( IResumeWithoutSignal target ) {
		fTarget = target;
	}
}
