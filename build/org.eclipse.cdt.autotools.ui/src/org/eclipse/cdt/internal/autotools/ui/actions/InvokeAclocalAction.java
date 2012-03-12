/*******************************************************************************
 * Copyright (c) 2006, 2007 2009 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.actions;

import org.eclipse.cdt.internal.autotools.core.AutotoolsPropertyConstants;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Shell;


public class InvokeAclocalAction extends InvokeAction {

	private static final String DEFAULT_OPTION = ""; //$NON-NLS-1$
	private static final String DEFAULT_COMMAND = "aclocal"; //$NON-NLS-1$

	public void run(IAction action) {

		IContainer container = getSelectedContainer();
		if (container == null)
			return;
		
		IPath execDir = getExecDir(container);
		String cwd = InvokeMessages.getString("CWD") + getCWD(container); //$NON-NLS-1$

		TwoInputDialog optionDialog = new TwoInputDialog(
				new Shell(),
				cwd,
				InvokeMessages
						.getString("InvokeAclocalAction.windowTitle.options"), //$NON-NLS-1$
				InvokeMessages
						.getString("InvokeAclocalAction.message.options.otherOptions"), //$NON-NLS-1$
				InvokeMessages
						.getString("InvokeAclocalAction.message.options.includeDir"), DEFAULT_OPTION, null); //$NON-NLS-1$

		optionDialog.open();

		// chop args into string array
		String rawArgList = optionDialog.getValue();

		String[] optionsList = separateOptions(rawArgList);

		// chop args into string array
		rawArgList = optionDialog.getSecondValue();

		String[] targetList = separateTargets(rawArgList);

		if (targetList == null) {

			showError(InvokeMessages
					.getString("InvokeAction.execute.windowTitle.error"), //$NON-NLS-1$
					InvokeMessages
							.getString("InvokeAction.windowTitle.quoteError")); //$NON-NLS-1$
			return;
		}

		int iOption = 0;
		if (targetList.length > 0)
			iOption = 1;

		String[] argumentList = new String[targetList.length
				+ optionsList.length + iOption];

		System.arraycopy(optionsList, 0, argumentList, 0, optionsList.length);

		if (iOption == 1)
			argumentList[optionsList.length] = "-I"; //$NON-NLS-1$

		System.arraycopy(targetList, 0, argumentList, optionsList.length
				+ iOption, targetList.length);

		if (container != null) {
			String aclocalCommand = null;
			IProject project = getSelectedContainer().getProject();
			try {
				aclocalCommand = project.getPersistentProperty(AutotoolsPropertyConstants.ACLOCAL_TOOL);
			} catch (CoreException e) {
				// do nothing
			}
			
			// If unset, use default system path
			if (aclocalCommand == null)
				aclocalCommand = DEFAULT_COMMAND;
			
			executeConsoleCommand(DEFAULT_COMMAND, aclocalCommand,
					argumentList, execDir);
		}
	}

	public void dispose() {

	}

}
