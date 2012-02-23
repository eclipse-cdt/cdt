/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Ruiz (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui.externaltool;

import static org.eclipse.ui.console.IConsoleConstants.ID_CONSOLE_VIEW;

import org.eclipse.cdt.codan.core.externaltool.IConsolePrinter;
import org.eclipse.cdt.codan.core.externaltool.IConsolePrinterProvider;
import org.eclipse.cdt.codan.internal.ui.CodanUIActivator;
import org.eclipse.cdt.codan.ui.CodanEditorUtility;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;

/**
 * Default implementation of <code>{@link IConsolePrinterProvider}</code>.
 * 
 * @author alruiz@google.com (Alex Ruiz)
 */
public class ConsolePrinterProvider implements IConsolePrinterProvider {
	private static final NullConsolePrinter NULL_CONSOLE = new NullConsolePrinter();
	
	@Override
	public IConsolePrinter createConsole(String externalToolName, boolean shouldDisplayOutput) {
		if (shouldDisplayOutput) {
			try {
				return createOrFindConsole(externalToolName);
			} catch (Throwable e) {
				CodanUIActivator.log("Unable to create/find console", e); //$NON-NLS-1$
			}
		}
		return NULL_CONSOLE;
	}

	private IConsolePrinter createOrFindConsole(String externalToolName) throws PartInitException {
		MessageConsole console = findConsole(externalToolName);
		IWorkbenchPage page = CodanEditorUtility.getActivePage();
		if (page != null) {
			IConsoleView view = (IConsoleView) page.showView(ID_CONSOLE_VIEW);
			view.display(console);
		}
		return new ConsolePrinter(console);
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
}
