/*******************************************************************************
 * Copyright (c) 2012 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alex Ruiz  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.ui.externaltool;

import static org.eclipse.ui.console.IConsoleConstants.ID_CONSOLE_VIEW;

import java.io.IOException;

import org.eclipse.cdt.codan.ui.CodanEditorUtility;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * Default implementation of <code>{@link ConsolePrinter}</code>.
 *
 * @author alruiz@google.com (Alex Ruiz)
 */
class ConsolePrinterImpl implements ConsolePrinter {
	private final MessageConsole console;
	private final MessageConsoleStream out;

	static ConsolePrinter createOrFindConsole(String externalToolName) 
			throws PartInitException {
		MessageConsole console = findConsole(externalToolName);
		IWorkbenchPage page = CodanEditorUtility.getActivePage();
		if (page != null) {
			IConsoleView view = (IConsoleView) page.showView(ID_CONSOLE_VIEW);
			view.display(console);
		}
		return new ConsolePrinterImpl(console);
	}

	private static MessageConsole findConsole(String externalToolName) {
		IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
		for (IConsole console : consoleManager.getConsoles()) {
			if (externalToolName.equals(console.getName()) && console instanceof MessageConsole) {
				return (MessageConsole) console;
			}
		}
		MessageConsole console = new MessageConsole(externalToolName, null);
		consoleManager.addConsoles(new IConsole[] { console });
		return console;
	}

	private ConsolePrinterImpl(MessageConsole console) {
		this.console = console;
		out = console.newMessageStream();
	}

	public void clear() {
		console.clearConsole();
	}
	
	public void println(String s) {
		out.println(s);
	}
	
	public void println() {
		out.println();
	}

	public void close() {
		try {
			out.close();
		} catch (IOException ignored) {}
	}
}
