/*******************************************************************************
 * Copyright (c) 2000, 2014 QNX Software Systems and others.
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
 *     Martin Oberhuber (Wind River) - [303083] Split out the Spawner
 *******************************************************************************/
package org.eclipse.cdt.internal.core.win32;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.IProcessInfo;
import org.eclipse.cdt.core.IProcessList;

import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.PsapiUtil;
import com.sun.jna.platform.win32.Win32Exception;

public class ProcessList implements IProcessList {
	private IProcessInfo[] NOPROCESS = new IProcessInfo[0];

	@Override
	public IProcessInfo[] getProcessList() {
		try {
			List<IProcessInfo> processList = new ArrayList<>();

			for (int pid : PsapiUtil.enumProcesses()) {
				try {
					String name = Kernel32Util.QueryFullProcessImageName(pid, 0);
					processList.add(new ProcessInfo(pid, name));
				} catch (Win32Exception e) {
					// Intentionally ignored exception. Probable cause is access denied.
				}
			}

			return processList.toArray(new IProcessInfo[processList.size()]);
		} catch (Win32Exception e) {
			return NOPROCESS;
		}
	}
}
