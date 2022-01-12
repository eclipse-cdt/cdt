/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.internal.console.actions;

import org.eclipse.remote.console.actions.ConsoleAction;
import org.eclipse.remote.internal.console.ImageConsts;
import org.eclipse.remote.internal.console.TerminalConsole;

public class ConsoleActionConnect extends ConsoleAction {
	private final TerminalConsole console;

	public ConsoleActionConnect(TerminalConsole console) {
		super(ConsoleActionConnect.class.getName());

		this.console = console;

		setupAction(ActionMessages.CONNECT, ActionMessages.CONNECT, ImageConsts.IMAGE_CLCL_CONNECT,
				ImageConsts.IMAGE_ELCL_CONNECT, ImageConsts.IMAGE_DLCL_CONNECT, true);
	}

	@Override
	public void run() {
		console.getTerminalConnector().connect();
	}
}
