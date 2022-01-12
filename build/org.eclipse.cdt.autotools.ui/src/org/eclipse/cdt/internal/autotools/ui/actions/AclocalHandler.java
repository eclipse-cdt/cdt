/*******************************************************************************
 * Copyright (c) 2008, 2015 Red Hat Inc..
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

/**
 * @author Jeff Johnston
 *
 */
public class AclocalHandler extends AbstractAutotoolsHandler {

	@Override
	public Object execute(ExecutionEvent event) {
		return execute1(event);
	}

	private static final String DEFAULT_OPTION = ""; //$NON-NLS-1$
	private static final String DEFAULT_COMMAND = "aclocal"; //$NON-NLS-1$

	@Override
	protected void run(Shell activeShell) {

		IContainer container = getSelectedContainer();
		if (container == null) {
			return;
		}

		IPath execDir = getExecDir(container);
		String cwd = InvokeMessages.getString("CWD") + getCWD(container); //$NON-NLS-1$

		TwoInputDialog optionDialog = new TwoInputDialog(activeShell, cwd,
				InvokeMessages.getString("InvokeAclocalAction.windowTitle.options"), //$NON-NLS-1$
				InvokeMessages.getString("InvokeAclocalAction.message.options.otherOptions"), //$NON-NLS-1$
				InvokeMessages.getString("InvokeAclocalAction.message.options.includeDir"), DEFAULT_OPTION, null); //$NON-NLS-1$

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

		int iOption = 0;
		if (targetList.size() > 0) {
			iOption = 1;
		}

		List<String> argumentList = new ArrayList<>();

		argumentList.addAll(optionsList);

		if (iOption == 1)
			argumentList.add("-I"); //$NON-NLS-1$

		argumentList.addAll(targetList);
		String aclocalCommand = null;
		IProject project = getSelectedContainer().getProject();
		try {
			aclocalCommand = project.getPersistentProperty(AutotoolsPropertyConstants.ACLOCAL_TOOL);
		} catch (CoreException e) {
			// do nothing
		}

		// If unset, use default system path
		if (aclocalCommand == null) {
			aclocalCommand = DEFAULT_COMMAND;
		}

		executeConsoleCommand(DEFAULT_COMMAND, aclocalCommand, argumentList, execDir);
	}

}
