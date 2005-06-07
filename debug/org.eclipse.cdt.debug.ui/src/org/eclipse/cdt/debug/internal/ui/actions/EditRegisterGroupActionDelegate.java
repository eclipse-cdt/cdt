/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/ 
package org.eclipse.cdt.debug.internal.ui.actions; 

import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.model.IPersistableRegisterGroup;
import org.eclipse.cdt.debug.core.model.IRegisterDescriptor;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionDelegate;
 

public class EditRegisterGroupActionDelegate extends ActionDelegate implements IObjectActionDelegate {

	private IPersistableRegisterGroup fSelection;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart( IAction action, IWorkbenchPart targetPart ) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged( IAction action, ISelection selection ) {
		if ( selection instanceof IStructuredSelection ) {
			IStructuredSelection ss = (IStructuredSelection)selection;
			if ( !ss.isEmpty() ) {
				Object s = ss.getFirstElement();
				if ( s instanceof IPersistableRegisterGroup ) {
					fSelection = (IPersistableRegisterGroup)s;
				}
			}
		}
	}

	private IPersistableRegisterGroup getRegisterGroup() {
		return fSelection;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run( IAction action ) {
		IPersistableRegisterGroup group = getRegisterGroup();
		IRegisterDescriptor[] all;
		try {
			all = ((CDebugTarget)group.getDebugTarget()).getRegisterDescriptors();
			RegisterGroupDialog dialog = new RegisterGroupDialog( Display.getCurrent().getActiveShell(), group.getName(), all, group.getRegisterDescriptors() );
			if ( dialog.open() == Window.OK ) {
				IDebugTarget target = group.getDebugTarget();
				if ( target instanceof ICDebugTarget ) {
					((ICDebugTarget)target).modifyRegisterGroup( group, dialog.getDescriptors() );
				}
			}
		}
		catch( DebugException e ) {
			CDebugUIPlugin.errorDialog( ActionMessages.getString( "EditRegisterGroupActionDelegate.0" ), e.getStatus() ); //$NON-NLS-1$
		}
	}
}
