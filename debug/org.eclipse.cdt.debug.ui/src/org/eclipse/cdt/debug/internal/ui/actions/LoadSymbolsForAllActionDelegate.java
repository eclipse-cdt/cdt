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

import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.jface.action.IAction;

/**
 * The delegate for the "Load Symbols For All" action of the Shared Libraries view.
 */
public class LoadSymbolsForAllActionDelegate extends AbstractViewActionDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractViewActionDelegate#getErrorDialogTitle()
	 */
	protected String getErrorDialogTitle() {
		return ActionMessages.getString( "LoadSymbolsForAllActionDelegate.Error_1" ); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractViewActionDelegate#getErrorDialogMessage()
	 */
	protected String getErrorDialogMessage() {
		return ActionMessages.getString( "LoadSymbolsForAllActionDelegate.Error(s)_occurred_loading_the_symbols_1" ); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractViewActionDelegate#doAction()
	 */
	protected void doAction() throws DebugException {
		ICDebugTarget target = getDebugTarget( getView().getViewer().getInput() );
		if ( target != null ) {
			target.loadSymbols();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractViewActionDelegate#update()
	 */
	protected void update() {
		IAction action = getAction();
		if ( getView() != null && action != null ) {
			ICDebugTarget target = getDebugTarget( getView().getViewer().getInput() );
			action.setEnabled( ( target != null ) ? target.isSuspended() : false );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractViewActionDelegate#doHandleDebugEvent(org.eclipse.debug.core.DebugEvent)
	 */
	protected void doHandleDebugEvent( DebugEvent event ) {
	}

	private ICDebugTarget getDebugTarget( Object element ) {
		if ( element instanceof IDebugElement ) {
			return (ICDebugTarget)((IDebugElement)element).getDebugTarget().getAdapter( ICDebugTarget.class );
		}
		return null;
	}
}
