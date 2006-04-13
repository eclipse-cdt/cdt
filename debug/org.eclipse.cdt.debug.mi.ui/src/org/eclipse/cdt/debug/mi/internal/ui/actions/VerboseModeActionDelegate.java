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
package org.eclipse.cdt.debug.mi.internal.ui.actions; 

import org.eclipse.cdt.debug.mi.core.GDBProcess;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionDelegate;
 
public class VerboseModeActionDelegate extends ActionDelegate implements IObjectActionDelegate {

	private GDBProcess fProcess;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart( IAction action, IWorkbenchPart targetPart ) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run( IAction action ) {
		if ( fProcess != null ) {
			boolean enabled = fProcess.getTarget().isVerboseModeEnabled();
			fProcess.getTarget().enableVerboseMode( !enabled );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged( IAction action, ISelection selection ) {
		IStructuredSelection s = (IStructuredSelection)selection;
		fProcess = ( !s.isEmpty() ) ? (GDBProcess)s.getFirstElement() : null;
		action.setEnabled( fProcess != null );
		action.setChecked( fProcess != null && fProcess.getTarget().isVerboseModeEnabled() );
	}
}
