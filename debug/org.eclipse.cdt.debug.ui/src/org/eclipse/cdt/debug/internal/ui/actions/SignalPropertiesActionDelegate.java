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
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.dialogs.PropertyDialogAction;

/**
 * Signal Properties action delegate.
 */
public class SignalPropertiesActionDelegate extends ActionDelegate implements IObjectActionDelegate {

	private ICSignal fSignal;

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
		PropertyDialogAction propertyAction = new PropertyDialogAction( CDebugUIPlugin.getActiveWorkbenchShell(), new ISelectionProvider() {

			public void addSelectionChangedListener( ISelectionChangedListener listener ) {
			}

			public ISelection getSelection() {
				return new StructuredSelection( getSignal() );
			}

			public void removeSelectionChangedListener( ISelectionChangedListener listener ) {
			}

			public void setSelection( ISelection selection ) {
			}
		} );
		propertyAction.run();
	}
}
