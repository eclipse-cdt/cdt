/*******************************************************************************
 * Copyright (c) 2009, 2015 Red Hat Inc..
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.actions;

import java.util.ArrayList;

import org.eclipse.cdt.internal.autotools.core.AutotoolsPropertyConstants;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.widgets.Shell;

public class AutoconfHandler extends AbstractAutotoolsHandler {

	@Override
	public Object execute(ExecutionEvent event) {
		return execute1(event);
	}

	private final static String DEFAULT_COMMAND = "autoconf"; //$NON-NLS-1$

	@Override
	public void run(Shell activeShell) {
		IContainer container = getSelectedContainer();
		if (container == null)
			return;

		IPath execDir = getExecDir(container);

		IProject project = container.getProject();
		String autoconfCommand = null;
		try {
			autoconfCommand = project.getPersistentProperty(AutotoolsPropertyConstants.AUTOCONF_TOOL);
		} catch (CoreException e) {
			// do nothing
		}

		// If unset for the project, default to system path
		if (autoconfCommand == null) {
			autoconfCommand = DEFAULT_COMMAND;
		}
		executeConsoleCommand(DEFAULT_COMMAND, autoconfCommand, new ArrayList<>(), execDir);
	}

}
