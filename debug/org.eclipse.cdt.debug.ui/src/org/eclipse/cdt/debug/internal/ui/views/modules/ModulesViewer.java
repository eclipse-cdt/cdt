/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.modules; 

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.model.viewers.AsynchronousTreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.progress.UIJob;
 
/**
 * Asynchronous viewer used by the Modules view.
 */
public class ModulesViewer extends AsynchronousTreeModelViewer {
	
	protected ModulesView fView;

	private UIJob fRestoreJob = new UIJob( "restore viewer state" ) { //$NON-NLS-1$

		public IStatus runInUIThread( IProgressMonitor monitor ) {
			fView.restoreState();
			return Status.OK_STATUS;
		}
	};

	/** 
	 * Constructor for ModulesViewer. 
	 */
	public ModulesViewer( Composite parent, ModulesView view ) {
		super( parent );
		fView = view;
		fRestoreJob.setSystem( true );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.viewers.AsynchronousModelViewer#updateComplete(org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor)
	 */
	protected void updateComplete( IAsynchronousRequestMonitor update ) {
		super.updateComplete( update );
		if ( fView != null ) {
			fRestoreJob.schedule( 100 );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.viewers.AsynchronousModelViewer#handlePresentationFailure(org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor, org.eclipse.core.runtime.IStatus)
	 */
	protected void handlePresentationFailure( IAsynchronousRequestMonitor update, IStatus status ) {
		fView.showMessage( status.getMessage() );
	}
}
