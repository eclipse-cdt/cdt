/*******************************************************************************
 * Copyright (c) 2011, 2011 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.make.internal.ui.scannerconfig;

import java.io.IOException;
import java.net.URL;

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.internal.core.ICConsole;
import org.eclipse.cdt.internal.ui.language.settings.providers.LanguageSettingsProviderAssociationManager;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class ScannerDiscoveryGlobalConsole implements ICConsole {
	private MessageConsole console;
	private ConsoleOutputStreamAdapter stream;

	private class ConsoleOutputStreamAdapter extends ConsoleOutputStream {
		private MessageConsoleStream fConsoleStream;
		public ConsoleOutputStreamAdapter(MessageConsoleStream stream) {
			fConsoleStream = stream;
		}
		@Override
		public void write(int arg0) throws IOException {
			fConsoleStream.write(arg0);
		}
		@Override
		public synchronized void write(byte[] b, int off, int len) throws IOException {
			fConsoleStream.write(b, off, len);
		}

		@Override
		public void flush() throws IOException {
			fConsoleStream.flush();
		}

		@Override
		public void close() throws IOException {
			fConsoleStream.close();
		}
	}

	@Override
	public void start(IProject project) {
		Assert.isTrue(project == null);
	}

	@Override
	public ConsoleOutputStream getOutputStream() throws CoreException {
		return stream;
	}

	@Override
	public ConsoleOutputStream getInfoStream() throws CoreException {
		return stream;
	}

	@Override
	public ConsoleOutputStream getErrorStream() throws CoreException {
		return stream;
	}

	@Override
	public void init(String consoleId, String name, URL defaultIconUrl) {
		console = null;

		IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
		IConsole[] allConsoles = consoleManager.getConsoles();
		for (IConsole con : allConsoles) {
			if (name.equals(con.getName()) && con instanceof MessageConsole) {
				console = (MessageConsole) con;
				console.clearConsole();
				break;
			}
		}

		if (console==null) {
			URL iconUrl = LanguageSettingsProviderAssociationManager.getImageUrl(consoleId);
			if (iconUrl==null) {
				iconUrl = defaultIconUrl;
			}

			console = new MessageConsole(name, CDTSharedImages.getImageDescriptor(iconUrl.toString()));
			console.activate();
			consoleManager.addConsoles(new IConsole[]{ console });
		}

		stream = new ConsoleOutputStreamAdapter(console.newMessageStream());
	}

}
