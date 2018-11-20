/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

public class Util {

	/**
	 * Returns an IStatus object with severity IStatus.ERROR based on the
	 * given Throwable.
	 * @param t the Throwable that caused the error.
	 * @return an IStatus object based on the given Throwable.
	 */
	public static IStatus createStatus(Throwable t) {
		String msg = t.getMessage();
		if (msg == null) {
			msg = Messages.Util_unexpectedError;
		}
		return new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, 0, msg, t);
	}

	/**
	 * Determines if [filename] is an absolute path specification on the host OS. For example, "c:\some\file"
	 * will return true on Windows, but false on UNIX. Conversely, "/some/file" will return false on Windows,
	 * true on Linux. "somefile.txt", "some/file", "./some/file", and "../some/file" will all return false on
	 * all hosts.
	 *
	 * <p>
	 * UNC paths ("\\some\dir") are recognized as native on Windows.
	 *
	 * @param filename
	 *            a file specification. Slashes do not need to be in native format or consistent, except for a
	 *            UNC path, where both prefix slashes must be either forward or backwards.
	 */
	public static boolean isNativeAbsolutePath(String filename) {
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			if (filename.length() > 2) {
				// "c:\some\dir"
				if (filename.charAt(1) == ':') {
					return filename.length() > 3 && isSlash(filename.charAt(2));
				} else {
					return filename.startsWith("\\\\") || // UNC //$NON-NLS-1$
							filename.startsWith("//"); // UNC converted to forward slashes //$NON-NLS-1$
				}
			}
			return false;
		} else {
			// So much simpler on Linux/UNIX (and MacOS now?)
			return filename.length() > 1 && isSlash(filename.charAt(0));
		}
	}

	private static boolean isSlash(Character c) {
		return c == '\\' || c == '/';
	}
}
