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

import org.eclipse.cdt.ui.newui.IConfigManager;
import org.eclipse.cdt.ui.newui.ManageConfigSelector;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler for command which lets to manage (add/remove etc.) build configurations of the project.
 */
public class ManageConfigsHandler extends AbstractHandler {

	public static final String COMMAND_ID = "org.eclipse.cdt.ui.menu.wsselection.command"; //$NON-NLS-1$

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = HandlerUtil.getCurrentStructuredSelection(event);
		if (!selection.isEmpty()) {
			IProject[] obs = ManageConfigSelector.getProjects(selection.toArray());
			IConfigManager cm = ManageConfigSelector.getManager(obs);
			if (cm != null && obs != null) {
				cm.manage(obs, true);
			}
		}
		return null;
	}
}
