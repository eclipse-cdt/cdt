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

import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * A delegate for the "Load Symbols For All" action of the Shared Libraries view.
 */
public class LoadSymbolsForAllActionDelegate extends ActionDelegate implements IViewActionDelegate, IUpdate {

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

	protected IAction getAction() {
		return fAction;
	}

	private void setAction( IAction action ) {
		fAction = action;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		IAction action = getAction();
		if ( getView() != null && action != null ) {
			ICDebugTarget target = getDebugTarget( getView().getViewer().getInput() );
			action.setEnabled( ( target != null ) ? target.isSuspended() : false );
		}
	}

	private ICDebugTarget getDebugTarget( Object element ) {
		if ( element instanceof IDebugElement ) {
			return (ICDebugTarget)((IDebugElement)element).getDebugTarget().getAdapter( ICDebugTarget.class );
		}
		return null;
	}

	public void run( IAction action ) {
		ICDebugTarget target = getDebugTarget( getView().getViewer().getInput() );
		if ( target != null ) {
			try {
				target.loadSymbols();
			}
			catch( DebugException e ) {
				DebugUIPlugin.errorDialog( getView().getSite().getShell(), "Error", "Operation failed.", e.getStatus() );
			}
		}
	}
}
