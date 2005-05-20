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
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
 
/**
 * A delegate for the "Add register group" action.
 */
public class AddRegisterGroupActionDelegate extends AbstractViewActionDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractViewActionDelegate#getErrorDialogTitle()
	 */
	protected String getErrorDialogTitle() {
		return ActionMessages.getString( "AddRegisterGroupActionDelegate.0" ); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractViewActionDelegate#getErrorDialogMessage()
	 */
	protected String getErrorDialogMessage() {
		return ActionMessages.getString( "AddRegisterGroupActionDelegate.1" ); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractViewActionDelegate#doAction()
	 */
	protected void doAction() throws DebugException {
		RegisterGroupDialog dialog = new RegisterGroupDialog( getView().getSite().getShell(), getDebugTarget().getRegisterDescriptors() );
		if ( dialog.open() == Window.OK ) {
			getDebugTarget().addUserDefinedRegisterGroup( dialog.getName(), dialog.getDescriptors() );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractViewActionDelegate#update()
	 */
	protected void update() {
		IAction action = getAction();
		if ( action != null ) {
			ICDebugTarget target = getDebugTarget();
			action.setEnabled( ( target != null ) ? target.isSuspended() : false );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractViewActionDelegate#doHandleDebugEvent(org.eclipse.debug.core.DebugEvent)
	 */
	protected void doHandleDebugEvent( DebugEvent event ) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractViewActionDelegate#init(org.eclipse.jface.action.IAction)
	 */
	public void init( IAction action ) {
		super.init( action );
		Object element = DebugUITools.getDebugContext();
		setSelection( (element != null) ? new StructuredSelection( element ) : new StructuredSelection() );
		update();
	}

	private ICDebugTarget getDebugTarget() {
		Object element = getSelection().getFirstElement();
		if ( element instanceof IDebugElement ) {
			return (ICDebugTarget)((IDebugElement)element).getDebugTarget().getAdapter( ICDebugTarget.class );
		}
		return null;
	}
}
