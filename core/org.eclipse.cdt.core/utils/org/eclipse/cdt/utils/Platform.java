/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation (Corey Ashford)
 *     Anton Leherbauer (Wind River Systems)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.cdt.core.CCorePlugin;
import org.osgi.framework.Bundle;

/**
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class Platform {

	// This class duplicates all of the methods in org.eclipse.core.runtime.Platform
	// that are used by the CDT.  getOSArch() needs a few tweaks because the value returned
	// by org.eclipse.core.runtime.Platform.getOSArch represents what the JVM thinks the
	// architecture is.  In some cases, we may actually be running on a 64-bit machine,
	// but the JVM thinks it's running on a 32-bit machine.  Without this change, the CDT
	// will not handle 64-bit executables on some ppc64.  This method could easily be
	// extended to handle other platforms with similar issues.
	//
	// Unfortunately, the org.eclipse.core.runtime.Platform is final, so we cannot just
	// extend it and and then override the getOSArch method, so getBundle and getOS just
	// encapsulate calls to the same methods in org.eclipse.core.runtime.Platform.

	public static final String OS_LINUX = org.eclipse.core.runtime.Platform.OS_LINUX;

	private static String cachedArch = null;

	public static Bundle getBundle(String symbolicName) {
		return org.eclipse.core.runtime.Platform.getBundle(symbolicName);
	}

	public static String getOS() {
		return org.eclipse.core.runtime.Platform.getOS();
	}

	public static String getOSArch() {
		if (cachedArch == null) {
			String arch = org.eclipse.core.runtime.Platform.getOSArch();
			if (arch.equals(org.eclipse.core.runtime.Platform.ARCH_PPC)) {
				// Determine if the platform is actually a ppc64 machine
				Process unameProcess;
				String cmd[] = { "uname", "-p" }; //$NON-NLS-1$//$NON-NLS-2$

				try {
					unameProcess = Runtime.getRuntime().exec(cmd);

					InputStreamReader inputStreamReader = new InputStreamReader(unameProcess.getInputStream());
					BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
					String unameOutput = bufferedReader.readLine();
					if (unameOutput != null) {
						arch = unameOutput;
					}
					bufferedReader.close();
					unameProcess.waitFor(); // otherwise the process becomes a zombie
				} catch (IOException e) {
					CCorePlugin.log(e);
				} catch (InterruptedException exc) {
					// restore interrupted flag
					Thread.currentThread().interrupt();
				}
			} else if (arch.equals(org.eclipse.core.runtime.Platform.ARCH_X86)) {
				// Determine if the platform is actually a x86_64 machine
				Process unameProcess;
				String cmd[];
				if (org.eclipse.core.runtime.Platform.OS_WIN32.equals(getOS())) {
					cmd = new String[] { "cmd", "/d", "/c", "set", "PROCESSOR_ARCHITECTURE" }; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				} else {
					// We don't use "uname -p" since it returns "unknown" on some Linux systems.
					cmd = new String[] { "uname", "-m" }; //$NON-NLS-1$//$NON-NLS-2$
				}

				try {
					unameProcess = Runtime.getRuntime().exec(cmd);
					unameProcess.getOutputStream().close();
					unameProcess.getErrorStream().close();
					InputStreamReader inputStreamReader = new InputStreamReader(unameProcess.getInputStream());
					BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
					String unameOutput = bufferedReader.readLine();
					if (unameOutput != null && unameOutput.endsWith("64")) { //$NON-NLS-1$
						arch = org.eclipse.core.runtime.Platform.ARCH_X86_64;
					}
					bufferedReader.close();
					unameProcess.waitFor(); // otherwise the process becomes a zombie
				} catch (IOException e) {
					CCorePlugin.log(e);
				} catch (InterruptedException exc) {
					// restore interrupted flag
					Thread.currentThread().interrupt();
				}
			}
			cachedArch = arch;
		}
		return cachedArch;
	}
}
