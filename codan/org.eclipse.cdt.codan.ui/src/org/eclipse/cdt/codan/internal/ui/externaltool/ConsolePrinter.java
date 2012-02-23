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

import org.eclipse.cdt.codan.core.externaltool.IConsolePrinter;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import java.io.IOException;

/**
 * Default implementation of <code>{@link IConsolePrinter}</code>.
 *
 * @author alruiz@google.com (Alex Ruiz)
 */
class ConsolePrinter implements IConsolePrinter {
	private final MessageConsole console;
	private final MessageConsoleStream out;

	ConsolePrinter(MessageConsole console) {
		this.console = console;
		out = console.newMessageStream();
	}

	@Override
	public void clear() {
		console.clearConsole();
	}
	
	@Override
	public void println(String s) {
		out.println(s);
	}
	
	@Override
	public void println() {
		out.println();
	}

	@Override
	public void close() {
		try {
			out.close();
		} catch (IOException ignored) {}
	}
}
