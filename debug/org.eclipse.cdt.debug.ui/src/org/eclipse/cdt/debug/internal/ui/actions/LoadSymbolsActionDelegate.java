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

import org.eclipse.cdt.debug.core.model.ICSharedLibrary;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart( IAction action, IWorkbenchPart targetPart ) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	public void run( IAction action ) {
		if ( getSharedLibrary() != null ) {
			final MultiStatus ms = new MultiStatus( CDebugUIPlugin.getUniqueIdentifier(),
													DebugException.REQUEST_FAILED,
													ActionMessages.getString( "LoadSymbolsActionDelegate.Unable_to_load_symbols_of_shared_library_1" ), //$NON-NLS-1$
													null );
			BusyIndicator.showWhile( Display.getCurrent(), 
									new Runnable() {
										public void run() {
											try {
												doAction( getSharedLibrary() );
											}
											catch( DebugException e ) {
												ms.merge( e.getStatus() );
											}
										}
									} );
			if ( !ms.isOK() ) {
				IWorkbenchWindow window = CDebugUIPlugin.getActiveWorkbenchWindow();
				if ( window != null ) {
					CDebugUIPlugin.errorDialog( ActionMessages.getString( "LoadSymbolsActionDelegate.Operation_failed_1" ), ms ); //$NON-NLS-1$
				}
				else {
					CDebugUIPlugin.log( ms );
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
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
}
