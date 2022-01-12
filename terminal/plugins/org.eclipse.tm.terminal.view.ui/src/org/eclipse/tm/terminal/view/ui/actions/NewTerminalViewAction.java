/*******************************************************************************
 * Copyright (c) 2015, 2018 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.actions;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.tm.internal.terminal.control.actions.AbstractTerminalAction;
import org.eclipse.tm.terminal.view.ui.activator.UIPlugin;
import org.eclipse.tm.terminal.view.ui.interfaces.ITerminalsView;
import org.eclipse.tm.terminal.view.ui.interfaces.ImageConsts;
import org.eclipse.tm.terminal.view.ui.nls.Messages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * Opens a new terminal view with a new secondary view ID.
 *
 * @since 4.1
 */
public class NewTerminalViewAction extends AbstractTerminalAction {

	//private ITerminalsView view = null;

	/**
	 * Constructor.
	 */
	public NewTerminalViewAction(ITerminalsView view) {
		super(null, NewTerminalViewAction.class.getName(), IAction.AS_PUSH_BUTTON);

		//this.view = view;
		setupAction(Messages.NewTerminalViewAction_menu, Messages.NewTerminalViewAction_tooltip,
				UIPlugin.getImageDescriptor(ImageConsts.ACTION_NewTerminalView_Hover),
				UIPlugin.getImageDescriptor(ImageConsts.ACTION_NewTerminalView_Enabled),
				UIPlugin.getImageDescriptor(ImageConsts.ACTION_NewTerminalView_Disabled), true);
		setEnabled(true);
	}

	@Override
	public void run() {
		ICommandService service = PlatformUI.getWorkbench().getService(ICommandService.class);
		Command command = service != null ? service.getCommand("org.eclipse.tm.terminal.view.ui.command.newview") //$NON-NLS-1$
				: null;
		if (command != null && command.isDefined() && command.isEnabled()) {
			try {
				ParameterizedCommand pCmd = ParameterizedCommand.generateCommand(command, null);
				Assert.isNotNull(pCmd);
				IHandlerService handlerSvc = PlatformUI.getWorkbench().getService(IHandlerService.class);
				Assert.isNotNull(handlerSvc);
				handlerSvc.executeCommandInContext(pCmd, null, handlerSvc.getCurrentState());
			} catch (Exception e) {
				// If the platform is in debug mode, we print the exception to the log view
				if (Platform.inDebugMode()) {
					IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(),
							Messages.AbstractTriggerCommandHandler_error_executionFailed, e);
					UIPlugin.getDefault().getLog().log(status);
				}
			}
		}
	}

}
