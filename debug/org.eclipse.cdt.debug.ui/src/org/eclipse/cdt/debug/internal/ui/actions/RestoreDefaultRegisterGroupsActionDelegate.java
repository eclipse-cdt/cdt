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

import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.StructuredSelection;
 

public class RestoreDefaultRegisterGroupsActionDelegate extends AbstractViewActionDelegate {

	@Override
	protected String getErrorDialogTitle() {
		return ActionMessages.getString( "RestoreDefaultRegisterGroupsActionDelegate.0" ); //$NON-NLS-1$
	}

	@Override
	protected String getErrorDialogMessage() {
		return ActionMessages.getString( "RestoreDefaultRegisterGroupsActionDelegate.1" ); //$NON-NLS-1$
	}

	@Override
	protected void doAction() throws DebugException {
		getDebugTarget().restoreDefaultRegisterGroups();
	}

	@Override
	protected void update() {
		IAction action = getAction();
		if ( action != null ) {
			ICDebugTarget target = getDebugTarget();
			action.setEnabled( ( target != null ) ? target.isSuspended() : false );
		}
	}

	@Override
	protected void doHandleDebugEvent( DebugEvent event ) {
	}

	private ICDebugTarget getDebugTarget() {
		Object element = getSelection().getFirstElement();
		if ( element instanceof IDebugElement ) {
			return (ICDebugTarget)((IDebugElement)element).getDebugTarget().getAdapter( ICDebugTarget.class );
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractViewActionDelegate#init(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void init( IAction action ) {
		super.init( action );
		Object element = DebugUITools.getDebugContext();
		setSelection( (element != null) ? new StructuredSelection( element ) : new StructuredSelection() );
		update();
	}
}
