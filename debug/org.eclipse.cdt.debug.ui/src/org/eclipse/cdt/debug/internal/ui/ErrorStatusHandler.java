/*******************************************************************************
 * Copyright (c) 2000, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jface.dialogs.ErrorDialog;

/**
 * Displays the error dialog.
 */
public class ErrorStatusHandler implements IStatusHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IStatusHandler#handleStatus(org.eclipse.core.runtime.IStatus, java.lang.Object)
	 */
	public Object handleStatus( final IStatus status, Object source ) throws CoreException {
		if ( status != null && source != null ) {
			String title = ""; //$NON-NLS-1$
			if (source instanceof IDebugElement) { 
				IDebugTarget target = ((IDebugElement)source).getDebugTarget();
				title = target.getName();
			}
			else {
				// Source is sometimes an action delegate instance. Can't gather 
				// anything useful from it. Use a generic title
				title = CDebugUIMessages.getString("ErrorStatusHandler.1"); //$NON-NLS-1$
			}
			final String title_f = title;
			CDebugUIPlugin.getStandardDisplay().asyncExec( new Runnable() {

				public void run() {
					ErrorDialog.openError( CDebugUIPlugin.getActiveWorkbenchShell(), title_f, null, status );
				}
			} );
		}
		return null;
	}
}
