/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
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
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.ActionDelegate;
 
/**
 * A delegate for the "Add Watchpoint" action.
 */
public class AddWatchpointActionDelegate extends ActionDelegate implements IViewActionDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init( IViewPart view ) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run( IAction action ) {
		AddWatchpointDialog dlg = new AddWatchpointDialog( CDebugUIPlugin.getActiveWorkbenchShell(), true, false, "", true ); //$NON-NLS-1$
		if ( dlg.open() == Window.OK ) {
			addWatchpoint( dlg.getWriteAccess(), dlg.getReadAccess(), dlg.getExpression() );
		}
	}

	private void addWatchpoint( boolean write, boolean read, String expression ) {
		if ( getResource() == null )
			return;
		try {
			CDIDebugModel.createWatchpoint( getSourceHandle(), getResource(), write, read, expression, true, 0, "", true ); //$NON-NLS-1$
		}
		catch( CoreException ce ) {
			CDebugUIPlugin.errorDialog( ActionMessages.getString( "AddWatchpointActionDelegate1.0" ), ce ); //$NON-NLS-1$
		}
	}

	private IResource getResource() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	private String getSourceHandle() {
		return ""; //$NON-NLS-1$
	}
}
