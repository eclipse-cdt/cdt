/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.model.ICSharedLibrary;
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

/**
 * Enter type comment.
 * 
 * @since: Jan 17, 2003
 */
public class LoadSymbolsActionDelegate implements IObjectActionDelegate {
	
	private ICSharedLibrary fLibrary;

	/**
	 * Constructor for LoadSymbolsActionDelegate.
	 */
	public LoadSymbolsActionDelegate() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart( IAction action, IWorkbenchPart targetPart ) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run( IAction action ) {
		final ICSharedLibrary library = getSharedLibrary(); 
		if ( library != null ) {
			
			DebugPlugin.getDefault().asyncExec( 
					new Runnable() {
						public void run() {
							try {
								doAction( getSharedLibrary() );
							}
							catch( DebugException e ) {
								failed( e );
							}
						}
					} );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged( IAction action, ISelection selection ) {
		if ( selection instanceof IStructuredSelection ) {
			Object element = ((IStructuredSelection)selection).getFirstElement();
			if ( element instanceof ICSharedLibrary ) {
				boolean enabled = enablesFor( (ICSharedLibrary)element );
				action.setEnabled( enabled );
				if ( enabled ) {
					setSharedLibrary( (ICSharedLibrary)element );
					return;
				}
			}
		}
		action.setEnabled( false );
		setSharedLibrary( null );
	}

	protected void doAction( ICSharedLibrary library ) throws DebugException {
		library.loadSymbols();
	}

	private boolean enablesFor(ICSharedLibrary library) {
		return ( library != null && !library.areSymbolsLoaded() );
	}

	private void setSharedLibrary( ICSharedLibrary library ) {
		fLibrary = library;
	}

	protected ICSharedLibrary getSharedLibrary() {
		return fLibrary;
	}

	protected void failed( Throwable e ) {
		MultiStatus ms = new MultiStatus( CDIDebugModel.getPluginIdentifier(), ICDebugInternalConstants.STATUS_CODE_ERROR, ActionMessages.getString( "LoadSymbolsActionDelegate.Operation_failed_1" ), null ); //$NON-NLS-1$
		ms.add( new Status( IStatus.ERROR, CDIDebugModel.getPluginIdentifier(), ICDebugInternalConstants.STATUS_CODE_ERROR, e.getMessage(), e ) );
		CDebugUtils.error( ms, this );
	}
}
