/*******************************************************************************
 * Copyright (c) 2008, 2009, 2012 Intel Corporation, QNX Software Systems, and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     QNX Software Systems - [272416] Rework the config sets dialog
 *     Freescale Semiconductor - [392954] disable the action if only empty working sets exist
 *******************************************************************************/
package org.eclipse.cdt.ui.actions;

import org.eclipse.cdt.internal.ui.workingsets.WorkingSetConfigurationDialog;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;

/**
 */
public class WorkingSetConfigAction implements IWorkbenchWindowActionDelegate, IPropertyChangeListener {

	private static final IWorkingSetManager wsm = CUIPlugin.getDefault().getWorkbench().getWorkingSetManager();
	private boolean enabled = true;
	private IWorkbenchWindow window;
	private IAction action;

	@Override
	public void run(IAction action) {
		this.action = action;
		checkWS();
		if (enabled) {
			new WorkingSetConfigurationDialog(window.getShell()).open();
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.action = action;
		checkWS();
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

	private void checkWS() {
		enabled = false;
		IWorkingSet[] w = wsm.getWorkingSets();
		if (w == null)
			w = new IWorkingSet[0];
		for (IWorkingSet ws : w) {
			if (!ws.isEmpty()) {
				enabled = true;
				break;
			}
		}
		if (action != null) {
			action.setEnabled(enabled);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		checkWS();
	}
}
