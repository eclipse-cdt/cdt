/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial API and implementation (Corey Ashford)
 *     
 *******************************************************************************/

package org.eclipse.cdt.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.osgi.framework.Bundle;

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
	
	private static boolean ppcArchIsCached = false;
	private static String cachedPpcArch = null;
	
	public static Bundle getBundle(String symbolicName) {
		return org.eclipse.core.runtime.Platform.getBundle(symbolicName);
	}
	
	public static String getOS() {
		return org.eclipse.core.runtime.Platform.getOS();
	}
	
	public static String getOSArch() {
		String arch = org.eclipse.core.runtime.Platform.getOSArch();
		if (arch.equals(org.eclipse.core.runtime.Platform.ARCH_PPC)) {
			// Determine if the platform is actually a ppc64 machine
			if (!ppcArchIsCached) {
				Process unameProcess;
				String cmd[] = {"uname", "-p"};

				ppcArchIsCached = true;
				try {
					unameProcess = Runtime.getRuntime().exec(cmd);

					InputStreamReader inputStreamReader = new InputStreamReader(unameProcess.getInputStream());
					BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
					cachedPpcArch = bufferedReader.readLine();

				} catch (IOException e) {
					cachedPpcArch = null;
				}
			}
			if (cachedPpcArch != null) {
				return cachedPpcArch;
			} else {
				return arch;
			}
		}
		return arch;
	}
}
