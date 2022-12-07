package org.eclipse.cdt.utils.pty;

import java.io.File;
import java.io.IOException;

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
		URL url = FileLocator.find(bundle, new Path("$os$/" + terminalEmulatorExeName), null); //$NON-NLS-1$
		if (url != null) {
			url = FileLocator.resolve(url);
			String path = url.getFile();
			File file = new File(path);
			if (file.exists()) {
				IPath p = Path.fromOSString(file.getCanonicalPath());
				command = p.toPortableString();
			}
		}

		if (command == null) {
			throw new IOException("can not find terminal emulator executable"); //$NON-NLS-1$
		}

		return command;
	}
}
