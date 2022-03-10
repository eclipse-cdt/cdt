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

import org.eclipse.jface.action.IAction;
import org.eclipse.remote.console.actions.ConsoleAction;
import org.eclipse.remote.internal.console.ImageConsts;
import org.eclipse.remote.internal.console.TerminalConsolePage;

public class ConsoleActionScrollLock extends ConsoleAction {
	private final TerminalConsolePage page;

	public ConsoleActionScrollLock(TerminalConsolePage page) {
		super(ConsoleActionScrollLock.class.getName(), IAction.AS_RADIO_BUTTON);

		this.page = page;

		setupAction(ActionMessages.SCROLL_LOCK, ActionMessages.SCROLL_LOCK, ImageConsts.IMAGE_CLCL_SCROLL_LOCK,
				ImageConsts.IMAGE_ELCL_SCROLL_LOCK, ImageConsts.IMAGE_DLCL_SCROLL_LOCK, true);
	}

	@Override
	public void run() {
		page.setScrollLock(!page.getScrollLock());
		setChecked(page.getScrollLock());
	}
}
