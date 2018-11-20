/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.linux;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;

import org.eclipse.cdt.core.IProcessInfo;
import org.eclipse.cdt.core.IProcessList;

/**
 * Insert the type's description here.
 * @see IProcessList
 */
public class ProcessList implements IProcessList {

	ProcessInfo[] empty = new ProcessInfo[0];

	public ProcessList() {
	}

	/**
	 * Insert the method's description here.
	 * @see IProcessList#getProcessList
	 */
	@Override
	public IProcessInfo[] getProcessList() {
		File proc = new File("/proc"); //$NON-NLS-1$
		File[] pidFiles = null;

		// We are only interrested in the pid so filter the rest out.
		try {
			FilenameFilter filter = new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					boolean isPID = false;
					try {
						Integer.parseInt(name);
						isPID = true;
					} catch (NumberFormatException e) {
					}
					return isPID;
				}
			};
			pidFiles = proc.listFiles(filter);
		} catch (SecurityException e) {
		}

		ProcessInfo[] processInfo = empty;
		if (pidFiles != null) {
			processInfo = new ProcessInfo[pidFiles.length];
			for (int i = 0; i < pidFiles.length; i++) {
				File cmdLine = new File(pidFiles[i], "cmdline"); //$NON-NLS-1$
				StringBuilder line = new StringBuilder();
				FileReader reader = null;
				try {
					reader = new FileReader(cmdLine);
					int c;
					while ((c = reader.read()) > 0) {
						line.append((char) c);
					}
				} catch (IOException e) {
				} finally {
					try {
						if (reader != null)
							reader.close();
					} catch (IOException e) {
						/* Don't care */}
					reader = null;
				}
				String name = line.toString();
				if (name.length() == 0) {
					name = "Unknown"; //$NON-NLS-1$
				}
				processInfo[i] = new ProcessInfo(pidFiles[i].getName(), name);
			}
		} else {
			pidFiles = new File[0];
		}
		return processInfo;
	}
}
