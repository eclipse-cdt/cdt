/*******************************************************************************
 * Copyright (c) 2008, 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.actions;

import org.eclipse.cdt.dsf.debug.ui.viewmodel.actions.AbstractVMProviderActionDelegate;
import org.eclipse.cdt.dsf.gdb.actions.IConnect;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewPart;

/*
 * Action to trigger a prompt for a process to attach to
 */
public class ConnectActionDelegate extends AbstractVMProviderActionDelegate {
	
	/*
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
    @Override
	public void run(IAction action) {
		if (action.isEnabled()) {
			// disable the action so it cannot be run again until an event or 
			// selection change updates the enablement
			action.setEnabled(false);

			final IConnect connectCommand = getConnectCommand();
			if (connectCommand != null) {
				connectCommand.connect(null);
			}
		}
	}

    @Override
    public void init(IViewPart view) {
        super.init(view);
		updateEnablement();
    }
    
    @Override
    public void debugContextChanged(DebugContextEvent event) {
        super.debugContextChanged(event);
		updateEnablement();
    }

    @Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		updateEnablement();
	}

	private void updateEnablement() {
		boolean enabled = false;
		final IConnect connectCommand = getConnectCommand();
		if (connectCommand != null) {
			enabled = connectCommand.canConnect();
		}
		getAction().setEnabled(enabled);
	}

	private IConnect getConnectCommand() {
		IConnect command = null;
		Object element = getViewerInput();
		if (element instanceof IDMVMContext) {
			IDMVMContext dmc = (IDMVMContext)element;
			command = (IConnect)dmc.getAdapter(IConnect.class);
		} else if (element instanceof GdbLaunch) {
			GdbLaunch launch = (GdbLaunch)element;
			command = (IConnect)launch.getSession().getModelAdapter(IConnect.class);
		}

		return command;
	}
}
