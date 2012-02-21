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

import java.io.IOException;

import org.eclipse.cdt.codan.core.externaltool.IConsolePrinter;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * Default implementation of <code>{@link IConsolePrinter}</code>.
 *
 * @author alruiz@google.com (Alex Ruiz)
 * 
 * @since 2.1
 */
class ConsolePrinter implements IConsolePrinter {
	private final MessageConsole console;
	private final MessageConsoleStream out;

	ConsolePrinter(MessageConsole console) {
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
