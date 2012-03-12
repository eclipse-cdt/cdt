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


/**
 * Class responsible for invoking automake.
 * 
 * @author klee
 * 
 */
public class InvokeAutomakeAction extends InvokeAction {

	private static final String DEFAULT_OPTION = ""; //$NON-NLS-1$
	private static final String DEFAULT_COMMAND = "automake"; //$NON-NLS-1$

	public void run(IAction action) {

		IContainer container = getSelectedContainer();
		if (container == null)
			return;
		
		IPath execDir = getExecDir(container);
		String cwd = InvokeMessages.getString("CWD") + getCWD(container); //$NON-NLS-1$
;
		TwoInputDialog optionDialog = new TwoInputDialog(new Shell(), cwd,
		InvokeMessages
		.getString("InvokeAutomakeAction.windowTitle.options"), //$NON-NLS-1$
InvokeMessages
		.getString("InvokeAutomakeAction.message.options.otherOptions"),InvokeMessages //$NON-NLS-1$
		.getString("InvokeAutomakeAction.message.options.makeTargets"), DEFAULT_OPTION, null); //$NON-NLS-1$
		
		optionDialog.open();
		
		// chop args into string array
		String rawArgList = optionDialog.getValue();

		String[] optionsList = separateOptions(rawArgList);


		// chop args into string array
		rawArgList = optionDialog.getSecondValue();

		String[] targetList = separateTargets(rawArgList);
		
		if (targetList == null) {

			showError(InvokeMessages.getString("InvokeAction.execute.windowTitle.error"), //$NON-NLS-1$
					InvokeMessages.getString("InvokeAction.windowTitle.quoteError")); //$NON-NLS-1$
			return;
		}

		String[] argumentList = new String[targetList.length
				+ optionsList.length];

		System.arraycopy(optionsList, 0, argumentList, 0, optionsList.length);
		System.arraycopy(targetList, 0, argumentList, optionsList.length,
				targetList.length);

		if (container != null) {
			IProject project = container.getProject();
			String automakeCommand = null;
			try {
				automakeCommand = project.getPersistentProperty(AutotoolsPropertyConstants.AUTOMAKE_TOOL);
			} catch (CoreException e) {
				// do nothing
			}
			
			// If automake path not set for the project, default to system path
			if (automakeCommand == null)
				automakeCommand = DEFAULT_COMMAND;

			executeConsoleCommand(DEFAULT_COMMAND, automakeCommand,
					argumentList, execDir);
		}
	}

	public void dispose() {

	}

}
