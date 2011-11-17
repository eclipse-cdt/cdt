/*******************************************************************************
 * Copyright (c) 2008, 2009 Intel Corporation, QNX Software Systems, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     QNX Software Systems - [272416] Rework the config sets dialog
 *******************************************************************************/
package org.eclipse.cdt.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;

import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.workingsets.WorkingSetConfigurationDialog;

/**
 */
public class WorkingSetConfigAction implements IWorkbenchWindowActionDelegate, IPropertyChangeListener {
	private static final IWorkingSetManager wsm = CUIPlugin.getDefault().getWorkbench().getWorkingSetManager();
	private boolean enabled = true;

	private IWorkbenchWindow window;

	@Override
	public void run(IAction action) {
		new WorkingSetConfigurationDialog(window.getShell()).open();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		checkWS();
		if (action.isEnabled() != enabled)
			action.setEnabled(enabled);
	}
	@Override
	public void dispose() {
		wsm.removePropertyChangeListener(this);

	}
	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
		wsm.addPropertyChangeListener(this);
		checkWS();
	}

	private IWorkingSet[] checkWS() {
		IWorkingSet[] w = wsm.getWorkingSets();
		if (w == null)
			w = new IWorkingSet[0];
		enabled = w.length > 0;
		return w;
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		checkWS();
	}
}
