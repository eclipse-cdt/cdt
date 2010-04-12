/*******************************************************************************
 * Copyright (c) 2010 Marc-Andre Laperle and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import org.eclipse.cdt.dsf.gdb.launching.GDBProcess;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * A console page participant for both the gdb tracing console and the gdb CLI console
 * 
 * @since 2.1
 */
public class ConsolePageParticipant implements IConsolePageParticipant{
	
	public void init(IPageBookViewPage page, IConsole console) {
		if(console instanceof TracingConsole || isConsoleGdbCli(console))
		{
			TextConsole textConsole = (TextConsole) console;

			// Add the save console action
			IToolBarManager toolBarManager = page.getSite().getActionBars().getToolBarManager();
			toolBarManager.appendToGroup(IConsoleConstants.OUTPUT_GROUP, new Separator());
			ConsoleSaveAction saveConsole = new ConsoleSaveAction(textConsole);
			toolBarManager.appendToGroup(IConsoleConstants.OUTPUT_GROUP, saveConsole);
			toolBarManager.appendToGroup(IConsoleConstants.OUTPUT_GROUP, new Separator());
		}
	}

	/**
	 * Checks if the the console is the gdb CLI. We don't rely on the attached 
	 * process name. Instead we check if the process is an instance of GDBProcess
	 * 
	 * @param console The console to check
	 * @return true if the the console is the gdb CLI
	 */
	private boolean isConsoleGdbCli(IConsole console) {
		if(console instanceof org.eclipse.debug.ui.console.IConsole) {
			org.eclipse.debug.ui.console.IConsole debugConsole  = (org.eclipse.debug.ui.console.IConsole)console;
			return (debugConsole.getProcess() instanceof GDBProcess);
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		return null;
	}
	
	public void dispose() {
	}

	public void activated() {
	}

	public void deactivated() {
	}

}
