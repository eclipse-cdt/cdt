/*******************************************************************************
 * Copyright (c) 2020 Simeon Andreev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simeon Andreev - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import org.eclipse.cdt.internal.ui.workingsets.WorkingSetConfigurationDialog;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class WorkingSetConfigHandler extends AbstractHandler {

	public static final String COMMAND_ID = "org.eclipse.cdt.ui.menu.manage.configs.command"; //$NON-NLS-1$

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		boolean enabled = hasNonEmptyWorksets();
		if (enabled) {
			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
			new WorkingSetConfigurationDialog(window.getShell()).open();
		}
		return null;
	}

	// TODO: property tester and handler enablement state for this
	private boolean hasNonEmptyWorksets() {
		IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSet[] workingSets = workingSetManager.getWorkingSets();
		if (workingSets != null) {
			for (IWorkingSet workingSet : workingSets) {
				if (!workingSet.isEmpty()) {
					return true;
				}
			}
		}
		return false;
	}

}
