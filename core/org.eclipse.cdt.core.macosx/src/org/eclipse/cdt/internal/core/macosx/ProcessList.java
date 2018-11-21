/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
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
package org.eclipse.cdt.internal.core.macosx;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.eclipse.cdt.core.IProcessInfo;
import org.eclipse.cdt.core.IProcessList;
import org.eclipse.cdt.utils.spawner.ProcessFactory;

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
		Process ps;
		BufferedReader psOutput;
		String[] args = { "/bin/ps", "-a", "-c", "-x", "-o", "pid,command" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

		try {
			ps = ProcessFactory.getFactory().exec(args);
			psOutput = new BufferedReader(new InputStreamReader(ps.getInputStream()));
		} catch (Exception e) {
			return new IProcessInfo[0];
		}

		//Read the output and parse it into an array list
		ArrayList procInfo = new ArrayList();

		try {
			String lastline;
			while ((lastline = psOutput.readLine()) != null) {
				//The format of the output should be
				//PID space name

				lastline = lastline.trim();
				int index = lastline.indexOf(' ');
				if (index != -1) {
					String pidString = lastline.substring(0, index).trim();
					try {
						int pid = Integer.parseInt(pidString);
						String arg = lastline.substring(index + 1);
						procInfo.add(new ProcessInfo(pid, arg));
					} catch (NumberFormatException e) {
					}
				}
			}
			psOutput.close();
		} catch (Exception e) {
			/* Ignore */
		}

		ps.destroy();
		return (IProcessInfo[]) procInfo.toArray(new IProcessInfo[procInfo.size()]);
	}

}
