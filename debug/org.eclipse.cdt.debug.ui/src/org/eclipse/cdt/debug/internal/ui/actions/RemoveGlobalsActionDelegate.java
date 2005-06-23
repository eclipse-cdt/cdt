/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions; 

import java.util.ArrayList;
import java.util.Iterator;
import org.eclipse.cdt.debug.core.ICGlobalVariableManager;
import org.eclipse.cdt.debug.core.model.ICGlobalVariable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.ActionDelegate;
 
/**
 * A delegate for the "Remove Globals" action.
 */
public class RemoveGlobalsActionDelegate extends ActionDelegate implements IViewActionDelegate {

	private IAction fAction;

	private ISelection fSelection;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init( IViewPart view ) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	public void init( IAction action ) {
		setAction( action );
		update();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run( IAction action ) {
		ISelection selection = getSelection();
		if ( !(selection instanceof IStructuredSelection) )
			return;
		IStructuredSelection ss = (IStructuredSelection)selection;
		final Iterator it = ss.iterator();
		ArrayList list = new ArrayList( ss.size() );
		while( it.hasNext() ) {
			Object obj = it.next();
			if ( obj instanceof ICGlobalVariable )
				list.add( obj );
		}
		if ( list.size() == 0 )
			return;
		final ICGlobalVariable[] globals = (ICGlobalVariable[])list.toArray( new ICGlobalVariable[list.size()] );
		final ICGlobalVariableManager gvm = (ICGlobalVariableManager)globals[0].getDebugTarget().getAdapter( ICGlobalVariableManager.class );
		if ( gvm == null )
			return;
		Runnable r = new Runnable() {
							public void run() {
								gvm.removeGlobals( globals );
							}
						};
		DebugPlugin.getDefault().asyncExec( r );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged( IAction action, ISelection selection ) {
		setSelection( selection );
		update();
	}

	protected IAction getAction() {
		return fAction;
	}

	protected ISelection getSelection() {
		return fSelection;
	}

	private void setAction( IAction action ) {
		fAction = action;
	}

	private void setSelection( ISelection selection ) {
		fSelection = selection;
	}

	private void update() {
		IAction action = getAction();
		if ( action != null ) {
			ISelection selection = getSelection();
			boolean enabled = false;
			if ( selection instanceof IStructuredSelection ) {
				Iterator it = ((IStructuredSelection)selection).iterator();
				while( it.hasNext() ) {
					if ( it.next() instanceof ICGlobalVariable ) {
						enabled = true;
						break;
					}
				}
			}
			action.setEnabled( enabled );
		}	
	}
}
