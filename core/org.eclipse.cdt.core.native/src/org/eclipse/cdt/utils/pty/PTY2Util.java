/********************************************************************************
 * Copyright (c) 2022 徐持恒 Xu Chiheng
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.cdt.utils.pty;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.cdt.internal.core.natives.CNativePlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

public class PTY2Util {

	public static String getTerminalEmulatorCommand() throws IOException {
		String command = null;
		Bundle bundle = Platform.getBundle(CNativePlugin.PLUGIN_ID);

		String terminalEmulatorExeName = "konsole"; //$NON-NLS-1$
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			terminalEmulatorExeName = "mintty.exe"; //$NON-NLS-1$
		} else if (Platform.getOS().equals(Platform.OS_LINUX)) {
			terminalEmulatorExeName = "konsole"; //$NON-NLS-1$
		} else {
			throw new IOException("can not find terminal emulator executable of currrent platform"); //$NON-NLS-1$
		}
		/*{
			URL url = FileLocator.find(bundle, new Path("$os$/"), null); //$NON-NLS-1$
			if (url != null) {
				url = FileLocator.resolve(url);
				String path = url.getFile();
			}
		}*/
		URL url = FileLocator.find(bundle, new Path("$os$/" + terminalEmulatorExeName), null); //$NON-NLS-1$
		if (url != null) {
			url = FileLocator.resolve(url);
			String path = url.getFile();
			File file = new File(path);
			if (file.exists()) {
				IPath p = Path.fromOSString(file.getCanonicalPath());
				command = p.toPortableString();
				/*if (Platform.getOS().equals(Platform.OS_WIN32) && Platform.getOSArch().equals(Platform.ARCH_X86_64)
						&& WindowsGCC.isCygwin32()) {
					command = command.replaceAll("x86_64", "x86"); //$NON-NLS-1$ //$NON-NLS-2$
				}*/
			}
		}

		if (command == null) {
			throw new IOException("can not find terminal emulator executable"); //$NON-NLS-1$
		}

		return command;
	}

	public static String[] getTerminalEmulatorCommandArray(String[] commandArray) {
		String command = "konsole"; //$NON-NLS-1$
		try {
			command = PTY2Util.getTerminalEmulatorCommand();
		} catch (IOException e) {
		}
		String[] terminalEmulatorCommand;

		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			terminalEmulatorCommand = new String[] { command, "--hold=always", "--exec" }; //$NON-NLS-1$  //$NON-NLS-2$
		} else if (Platform.getOS().equals(Platform.OS_LINUX)) {
			terminalEmulatorCommand = new String[] { command, "--nofork", "--hold", "-e" }; //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
		} else {
			terminalEmulatorCommand = new String[] { command, "--nofork", "--hold", "-e" }; //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
		}

		String[] result = new String[terminalEmulatorCommand.length + commandArray.length];
		System.arraycopy(terminalEmulatorCommand, 0, result, 0, terminalEmulatorCommand.length);
		System.arraycopy(commandArray, 0, result, terminalEmulatorCommand.length, commandArray.length);

		return result;
	}
}
