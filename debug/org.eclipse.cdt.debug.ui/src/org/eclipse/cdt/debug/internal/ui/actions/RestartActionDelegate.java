/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Navid Mehregani (TI) - Bug 289526 - Migrate the Restart feature to the new one, as supported by the platform
 *     Wind River Systems - Bug 289526 - Additional fixes
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.model.IRestart;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.commands.IRestartHandler;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.internal.ui.commands.actions.RestartCommandAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * The delegate of the "Restart" action.
 */
public class RestartActionDelegate extends AbstractListenerActionDelegate {

	private RestartCommandAction fRestartCommandAction;

	@Override
	public void init(IAction action) {
	    setAction(action);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractDebugActionDelegate#doAction(java.lang.Object)
	 */
	protected void doAction( Object element ) throws DebugException {
		IRestartHandler asynchronousRestartHandler = getAsynchronousRestartHandler( element );
		if (asynchronousRestartHandler!=null && fRestartCommandAction!=null ) {
			fRestartCommandAction.run();
		} else {
			IRestart restartTarget = getRestartTarget( element );
			if ( restartTarget != null ) {
				restartTarget.restart();
			}	
			
		}
	}

	@Override
	public void init(IViewPart view) {
	    super.init(view);
        fRestartCommandAction = new RestartCommandAction();
        fRestartCommandAction.setActionProxy(getAction());
        fRestartCommandAction.init(getView());
	}
	
	@Override
	public void init(IWorkbenchWindow window) {
	    super.init(window);
        fRestartCommandAction = new RestartCommandAction();
        fRestartCommandAction.setActionProxy(getAction());
        fRestartCommandAction.init(getWindow());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractDebugActionDelegate#isEnabledFor(java.lang.Object)
	 */
	protected boolean isEnabledFor( Object element ) {
		IRestartHandler asynchronousRestartHandler = getAsynchronousRestartHandler( element );
		if (asynchronousRestartHandler!=null && fRestartCommandAction!=null) {
		    return fRestartCommandAction.isEnabled();
		} else {
			IRestart restartTarget = getRestartTarget( element );
			if ( restartTarget != null ) {
				return checkCapability( restartTarget );
			}
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
	
	protected IRestartHandler getAsynchronousRestartHandler( Object element ) {
		if ( element instanceof IAdaptable )
			return (IRestartHandler)((IAdaptable)element).getAdapter( IRestartHandler.class );
		
		return null;
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
