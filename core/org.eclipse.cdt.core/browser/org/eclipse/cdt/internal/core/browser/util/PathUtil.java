/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.browser.util;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.Path;

public class PathUtil {
	
	private static boolean fWindows = false;
	static {
		String os = System.getProperty("os.name"); //$NON-NLS-1$
		if (os != null && os.startsWith("Win")) { //$NON-NLS-1$
			fWindows= true;
		}
	}
	public static boolean isWindowsSystem() {
		return fWindows;
	}
	
	public static Path getCanonicalPath(String fullPath) {
		File file = new File(fullPath);
		try {
			String canonPath = file.getCanonicalPath();
			return new Path(canonPath);
		} catch (IOException ex) {
		}
		return new Path(fullPath);
	}
}
