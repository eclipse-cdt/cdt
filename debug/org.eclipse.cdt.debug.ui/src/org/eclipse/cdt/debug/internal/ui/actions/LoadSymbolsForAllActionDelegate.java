/*******************************************************************************
 * Copyright (c) 2004, 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.model.IModuleRetrieval;
import org.eclipse.cdt.debug.internal.core.ICDebugInternalConstants;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jface.action.IAction;

/**
 * The delegate for the "Load Symbols For All" action of the Modules view.
 */
public class LoadSymbolsForAllActionDelegate extends AbstractViewActionDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractViewActionDelegate#getErrorDialogTitle()
	 */
	@Override
	protected String getErrorDialogTitle() {
		return ActionMessages.getString( "LoadSymbolsForAllActionDelegate.Error_1" ); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractViewActionDelegate#getErrorDialogMessage()
	 */
	@Override
	protected String getErrorDialogMessage() {
		return ActionMessages.getString( "LoadSymbolsForAllActionDelegate.Error(s)_occurred_loading_the_symbols_1" ); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractViewActionDelegate#doAction()
	 */
	@Override
	protected void doAction() throws DebugException {
		ICDebugTarget target = getDebugTarget( getView().getViewer().getInput() );
		if ( target != null ) {
			final IModuleRetrieval mr = target.getAdapter( IModuleRetrieval.class );
			if ( mr != null ) {
				DebugPlugin.getDefault().asyncExec( 
					new Runnable() {
						
						@Override
						public void run() {
							try {
								mr.loadSymbolsForAllModules();
							}
							catch( DebugException e ) {
								failed( e );
							}
						}
					} );
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractViewActionDelegate#update()
	 */
	@Override
	protected void update() {
		IAction action = getAction();
		if ( getView() != null && getView().getViewer() != null && action != null ) {
			ICDebugTarget target = getDebugTarget( getView().getViewer().getInput() );
			action.setEnabled( ( target != null ) ? target.isSuspended() : false );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractViewActionDelegate#doHandleDebugEvent(org.eclipse.debug.core.DebugEvent)
	 */
	@Override
	protected void doHandleDebugEvent( DebugEvent event ) {
	}

	private ICDebugTarget getDebugTarget( Object element ) {
		if ( element instanceof IAdaptable ) {
			return ((IAdaptable)element).getAdapter( ICDebugTarget.class );
		}
		return null;
	}

	protected void failed( Throwable e ) {
		MultiStatus ms = new MultiStatus( CDIDebugModel.getPluginIdentifier(), ICDebugInternalConstants.STATUS_CODE_ERROR, ActionMessages.getString( "LoadSymbolsForAllActionDelegate.0" ), null ); //$NON-NLS-1$
		ms.add( new Status( IStatus.ERROR, CDIDebugModel.getPluginIdentifier(), ICDebugInternalConstants.STATUS_CODE_ERROR, e.getMessage(), e ) );
		CDebugUtils.error( ms, this );
	}
}
