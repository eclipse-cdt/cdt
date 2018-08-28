/*******************************************************************************
 * Copyright (c) 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.internal.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.tm.terminal.view.ui.interfaces.IUIConstants;
import org.eclipse.tm.terminal.view.ui.manager.ConsoleManager;

/**
 * New Terminal View handler implementation
 */
public class NewTerminalViewHandler extends AbstractTriggerCommandHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String secondaryId = ConsoleManager.getInstance().getNextTerminalSecondaryId(IUIConstants.ID);
		ConsoleManager.getInstance().showConsoleView(IUIConstants.ID, secondaryId);

		triggerCommand("org.eclipse.tm.terminal.view.ui.command.launchToolbar", null); //$NON-NLS-1$

		return null;
	}

}
