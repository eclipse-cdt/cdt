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

import java.util.Iterator;
import org.eclipse.cdt.debug.core.model.IEnableDisableTarget;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * The delegate of the "Enable" action contribution to the "IVariable" objects.
 */
public class EnableVariablesActionDelegate implements IViewActionDelegate {

	private IViewPart fView;

	private IAction fAction;

	public EnableVariablesActionDelegate() {
	}

	protected IViewPart getView() {
		return fView;
	}

	protected void setView( IViewPart view ) {
		fView = view;
	}

	protected IAction getAction() {
		return fAction;
	}

	protected void setAction( IAction action ) {
		fAction = action;
	}

	/**
	 * This action enables variables.
	 */
	protected boolean isEnableAction() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init( IViewPart view ) {
		setView( view );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run( IAction action ) {
		IStructuredSelection selection = getSelection();
		final int size = selection.size();
		if ( size == 0 )
			return;
		final Iterator it = selection.iterator();
		final MultiStatus ms = new MultiStatus( CDebugUIPlugin.getUniqueIdentifier(), DebugException.REQUEST_FAILED, ActionMessages.getString( "EnableVariablesActionDelegate.0" ), null ); //$NON-NLS-1$
		BusyIndicator.showWhile( Display.getCurrent(), new Runnable() {

			public void run() {
				while( it.hasNext() ) {
					IEnableDisableTarget target = getEnableDisableTarget( it.next() );
					if ( target != null ) {
						try {
							if ( size > 1 ) {
								target.setEnabled( isEnableAction() );
							}
							else
								target.setEnabled( !target.isEnabled() );
						}
						catch( DebugException e ) {
							ms.merge( e.getStatus() );
						}
					}
				}
				update();
			}
		} );
		if ( !ms.isOK() ) {
			CDebugUIPlugin.errorDialog( ActionMessages.getString( "EnableVariablesActionDelegate.1" ), ms ); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged( IAction action, ISelection selection ) {
		setAction( action );
		if ( !(selection instanceof IStructuredSelection) )
			return;
		IStructuredSelection sel = (IStructuredSelection)selection;
		Object o = sel.getFirstElement();
		if ( getEnableDisableTarget( o ) == null )
			return;
		Iterator it = sel.iterator();
		boolean allEnabled = true;
		boolean allDisabled = true;
		while( it.hasNext() ) {
			IEnableDisableTarget target = getEnableDisableTarget( it.next() );
			if ( target != null && !target.canEnableDisable() )
				continue;
			if ( target.isEnabled() )
				allDisabled = false;
			else
				allEnabled = false;
		}
		if ( isEnableAction() )
			action.setEnabled( !allEnabled );
		else
			action.setEnabled( !allDisabled );
	}

	private IStructuredSelection getSelection() {
		return (IStructuredSelection)getView().getViewSite().getSelectionProvider().getSelection();
	}

	protected void update() {
		getView().getViewSite().getSelectionProvider().setSelection( getView().getViewSite().getSelectionProvider().getSelection() );
	}

	protected IEnableDisableTarget getEnableDisableTarget( Object obj ) {
		IEnableDisableTarget target = null;
		if ( obj instanceof IAdaptable ) {
			target = (IEnableDisableTarget)((IAdaptable)obj).getAdapter( IEnableDisableTarget.class );
		}
		return target;
	}
}
