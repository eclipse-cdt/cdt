/*******************************************************************************
 * Copyright (c) 2011, 2012 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui.language.settings.providers;

import java.io.IOException;
import java.net.URL;

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.internal.core.ICConsole;
import org.eclipse.cdt.managedbuilder.language.settings.providers.AbstractBuiltinSpecsDetector;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.language.settings.providers.LanguageSettingsProvidersImages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * Console adapter for global {@link AbstractBuiltinSpecsDetector}.
 *
 * Note that this console is not colored.
 */
public class ScannerDiscoveryGlobalConsole implements ICConsole {
	private MessageConsole console;

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
		return new ConsoleOutputStreamAdapter(console.newMessageStream());
	}

	@Override
	public ConsoleOutputStream getInfoStream() throws CoreException {
		return new ConsoleOutputStreamAdapter(console.newMessageStream());
	}

	@Override
	public ConsoleOutputStream getErrorStream() throws CoreException {
		return new ConsoleOutputStreamAdapter(console.newMessageStream());
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
			URL iconUrl = LanguageSettingsProvidersImages.getImageUrl(consoleId);
			if (iconUrl == null) {
				iconUrl = defaultIconUrl;
			}

			ImageDescriptor imageDescriptor;
			if (iconUrl != null) {
				imageDescriptor = CDTSharedImages.getImageDescriptor(iconUrl.toString());
			} else {
				imageDescriptor = ImageDescriptor.getMissingImageDescriptor();
			}

			console = new MessageConsole(name, imageDescriptor);
			console.activate();
			consoleManager.addConsoles(new IConsole[]{ console });
		}
	}

}
