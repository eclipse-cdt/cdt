/*******************************************************************************
 * Copyright (c) 2011-2013 Nokia Siemens Networks Oyj, Finland.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Nokia Siemens Networks - initial implementation
 *      Petri Tuononen - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.llvm.util;

import java.io.File;

public class FileUtil {

	/**
	 * Searches cpp and cc file types recursively and returns true if
	 * the directory tree contains at least one instance of them.
	 *
	 * @param directory
	 * @param filter
	 * @return
	 */
	public static boolean containsCppFile(File directory) {
		File[] entries = directory.listFiles();
		String path = null;
		for (File entry : entries) {
			path = entry.getAbsolutePath();
			if (path!=null) {
				if (path.contains(".cpp") || path.contains(".cc")) { //$NON-NLS-1$ //$NON-NLS-2$
					return true;
				}
			}

			if (entry.isDirectory()) {
				containsCppFile(entry);
			}
		}
		return false;
	}

}
