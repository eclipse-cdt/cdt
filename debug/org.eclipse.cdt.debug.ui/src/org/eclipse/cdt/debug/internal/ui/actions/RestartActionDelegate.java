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

import org.eclipse.cdt.debug.core.model.IRestart;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;

/**
 * The delegate of the "Restart" action.
 */
public class RestartActionDelegate extends AbstractListenerActionDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractDebugActionDelegate#doAction(java.lang.Object)
	 */
	protected void doAction( Object element ) throws DebugException {
		IRestart restartTarget = getRestartTarget( element );
		if ( restartTarget != null ) {
			restartTarget.restart();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractDebugActionDelegate#isEnabledFor(java.lang.Object)
	 */
	protected boolean isEnabledFor( Object element ) {
		IRestart restartTarget = getRestartTarget( element );
		if ( restartTarget != null ) {
			return checkCapability( restartTarget );
		}
		return false;
	}

	protected boolean checkCapability( IRestart element ) {
		return element.canRestart();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractDebugActionDelegate#getStatusMessage()
	 */
	protected String getStatusMessage() {
		return ActionMessages.getString( "RestartActionDelegate.0" ); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractDebugActionDelegate#getErrorDialogMessage()
	 */
	protected String getErrorDialogMessage() {
		return ActionMessages.getString( "RestartActionDelegate.1" ); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractDebugActionDelegate#getErrorDialogTitle()
	 */
	protected String getErrorDialogTitle() {
		return ActionMessages.getString( "RestartActionDelegate.2" ); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractDebugActionDelegate#isRunInBackground()
	 */
	protected boolean isRunInBackground() {
		return true;
	}

	protected IRestart getRestartTarget( Object element ) {
		if ( element instanceof IAdaptable )
			return (IRestart)((IAdaptable)element).getAdapter( IRestart.class );
		return getDefaultRestartTarget( element );
	}

	private IRestart getDefaultRestartTarget( Object element ) {
		if ( element instanceof IDebugElement ) {
			IDebugTarget target = ((IDebugElement)element).getDebugTarget();
			if ( target instanceof IRestart )
				return (IRestart)target;
		}
		return null;
	}
}
