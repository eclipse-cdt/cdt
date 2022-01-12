/*******************************************************************************
 * Copyright (c) 2009,2015 Red Hat Inc..
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
import java.util.List;

import org.eclipse.cdt.internal.autotools.core.AutotoolsPropertyConstants;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.widgets.Shell;

public class AutomakeHandler extends AbstractAutotoolsHandler {

	@Override
	public Object execute(ExecutionEvent event) {
		return execute1(event);
	}

	private static final String DEFAULT_OPTION = ""; //$NON-NLS-1$
	private static final String DEFAULT_COMMAND = "automake"; //$NON-NLS-1$

	@Override
	public void run(Shell activeShell) {

		IContainer container = getSelectedContainer();
		if (container == null) {
			return;
		}

		IPath execDir = getExecDir(container);
		String cwd = InvokeMessages.getString("CWD") + getCWD(container); //$NON-NLS-1$
		TwoInputDialog optionDialog = new TwoInputDialog(activeShell, cwd,
				InvokeMessages.getString("InvokeAutomakeAction.windowTitle.options"), //$NON-NLS-1$
				InvokeMessages.getString("InvokeAutomakeAction.message.options.otherOptions"), //$NON-NLS-1$
				InvokeMessages.getString("InvokeAutomakeAction.message.options.makeTargets"), //$NON-NLS-1$
				DEFAULT_OPTION, null);

		optionDialog.open();

		// chop args into string array
		String rawArgList = optionDialog.getValue();

		List<String> optionsList = separateOptions(rawArgList);

		// chop args into string array
		rawArgList = optionDialog.getSecondValue();

		List<String> targetList = separateTargets(rawArgList);

		if (targetList == null) {

			showError(InvokeMessages.getString("InvokeAction.execute.windowTitle.error"), //$NON-NLS-1$
					InvokeMessages.getString("InvokeAction.windowTitle.quoteError")); //$NON-NLS-1$
			return;
		}

		List<String> argumentList = new ArrayList<>();
		argumentList.addAll(optionsList);
		argumentList.addAll(targetList);

		IProject project = container.getProject();
		String automakeCommand = null;
		try {
			automakeCommand = project.getPersistentProperty(AutotoolsPropertyConstants.AUTOMAKE_TOOL);
		} catch (CoreException e) {
			// do nothing
		}

		// If automake path not set for the project, default to system path
		if (automakeCommand == null) {
			automakeCommand = DEFAULT_COMMAND;
		}

		executeConsoleCommand(DEFAULT_COMMAND, automakeCommand, argumentList, execDir);
	}

}
