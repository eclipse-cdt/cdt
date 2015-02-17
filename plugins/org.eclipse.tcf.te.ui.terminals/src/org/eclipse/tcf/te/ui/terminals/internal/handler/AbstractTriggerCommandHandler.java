/*******************************************************************************
 * Copyright (c) 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.terminals.internal.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.tcf.te.ui.terminals.activator.UIPlugin;
import org.eclipse.tcf.te.ui.terminals.nls.Messages;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * Abstract command handler triggering a command to be executed.
 */
public abstract class AbstractTriggerCommandHandler extends AbstractHandler {

	/**
	 * Trigger a command to be executed.
	 *
	 * @param commandId The command id. Must not be <code>null</code>.
	 * @param selection The selection to pass on to the command or <code>null</code>.
	 */
	protected void triggerCommand(String commandId, ISelection selection) {
		Assert.isNotNull(commandId);

		ICommandService service = (ICommandService)PlatformUI.getWorkbench().getService(ICommandService.class);
		Command command = service != null ? service.getCommand(commandId) : null;
		if (command != null && command.isDefined() && command.isEnabled()) {
			try {
				ParameterizedCommand pCmd = ParameterizedCommand.generateCommand(command, null);
				Assert.isNotNull(pCmd);
				IHandlerService handlerSvc = (IHandlerService)PlatformUI.getWorkbench().getService(IHandlerService.class);
				Assert.isNotNull(handlerSvc);
				IEvaluationContext ctx = handlerSvc.getCurrentState();
				if (selection != null) {
					ctx = new EvaluationContext(ctx, selection);
					ctx.addVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME, selection);
				}
				handlerSvc.executeCommandInContext(pCmd, null, ctx);
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
