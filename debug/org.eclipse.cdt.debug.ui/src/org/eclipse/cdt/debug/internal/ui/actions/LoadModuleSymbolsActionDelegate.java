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

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.model.ICModule;
import org.eclipse.cdt.debug.internal.core.ICDebugInternalConstants;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionDelegate;

/**
 * The delegate of the "Load Symbols" action contribution to the "ICModule" objects.
 */
public class LoadModuleSymbolsActionDelegate extends ActionDelegate implements IObjectActionDelegate {

	private ICModule fModule;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart( IAction action, IWorkbenchPart targetPart ) {
	}

	protected ICModule getModule() {
		return fModule;
	}

	private void setModule( ICModule module ) {
		fModule = module;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run( IAction action ) {
		final ICModule module = getModule(); 
		if ( module != null ) {
			
			DebugPlugin.getDefault().asyncExec( 
					new Runnable() {
						public void run() {
							try {
								doAction( module );
							}
							catch( DebugException e ) {
								failed( e );
							}
						}
					} );
		}
	}

	protected void doAction( ICModule module ) throws DebugException {
		module.loadSymbols();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged( IAction action, ISelection selection ) {
		if ( selection instanceof IStructuredSelection ) {
			if ( ((IStructuredSelection)selection).size() == 1 ) {
				Object element = ((IStructuredSelection)selection).getFirstElement();
				if ( element instanceof ICModule ) {
					boolean enabled = enablesFor( (ICModule)element );
					action.setEnabled( enabled );
					if ( enabled ) {
						setModule( (ICModule)element );
						return;
					}
				}
			}
		}
		action.setEnabled( false );
		setModule( null );
	}

	private boolean enablesFor( ICModule module ) {
		return ( module != null && module.canLoadSymbols() );
	}

	protected void failed( Throwable e ) {
		MultiStatus ms = new MultiStatus( CDIDebugModel.getPluginIdentifier(), ICDebugInternalConstants.STATUS_CODE_ERROR, ActionMessages.getString( "LoadModuleSymbolsActionDelegate.0" ), null ); //$NON-NLS-1$
		ms.add( new Status( IStatus.ERROR, CDIDebugModel.getPluginIdentifier(), ICDebugInternalConstants.STATUS_CODE_ERROR, e.getMessage(), null ) );
		CDebugUtils.error( ms, getModule() );
	}
}
