/*******************************************************************************
 * Copyright (c) 2010, 2015 Broadcom Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Alex Collins (Broadcom Corp.) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.buildconsole;

import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * Attached to extension point org.eclipse.ui.console.consolePageParticipants to notify
 * BuildConsole that a new page has become visible.
 */
public class BuildConsolePageParticipant implements IConsolePageParticipant {
	private BuildConsole console;

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	@Override
	public void init(IPageBookViewPage page, IConsole console) {
		this.console = (BuildConsole) console;
	}

	@Override
	public void dispose() {
	}

	@Override
	public void activated() {
		BuildConsole.setCurrentPage(console.getPage());
	}

	@Override
	public void deactivated() {
	}
}
