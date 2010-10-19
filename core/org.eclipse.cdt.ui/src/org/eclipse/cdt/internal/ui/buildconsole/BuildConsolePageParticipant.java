/*******************************************************************************
 * Copyright (c) 2010 Broadcom Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		return null;
	}

	public void init(IPageBookViewPage page, IConsole console) {
		this.console = (BuildConsole)console;
	}

	public void dispose() {
	}

	public void activated() {
		BuildConsole.setCurrentPage(console.getPage());
	}

	public void deactivated() {
	}
}
