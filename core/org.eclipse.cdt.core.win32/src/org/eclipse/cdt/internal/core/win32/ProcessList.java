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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.cdt.core.IProcessInfo;
import org.eclipse.cdt.core.IProcessList;
import org.eclipse.cdt.internal.core.natives.CNativePlugin;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

/*
 * Currently this will only work for Windows XP since tasklist
 * is only shipped on XP. This could change to some JNI
 * call out to get the list since the source to 'tlist' is
 * on the msdn web site but that can be done later.
 */

public class ProcessList implements IProcessList {

	private IProcessInfo[] NOPROCESS = new IProcessInfo[0];

	@Override
	public IProcessInfo[] getProcessList() {
		Process p = null;
		String command = null;
		InputStream in = null;
		Bundle bundle = Platform.getBundle(CNativePlugin.PLUGIN_ID);
		IProcessInfo[] procInfos = NOPROCESS;

		try {
			URL url = FileLocator.find(bundle, new Path("$os$/listtasks.exe"), null); //$NON-NLS-1$
			if (url != null) {
				url = FileLocator.resolve(url);
				String path = url.getFile();
				File file = new File(path);
				if (file.exists()) {
					command = file.getCanonicalPath();
					if (command != null) {
						try {
							p = ProcessFactory.getFactory().exec(command);
							in = p.getInputStream();
							InputStreamReader reader = new InputStreamReader(in);
							procInfos = parseListTasks(reader);
						} finally {
							if (in != null)
								in.close();
							if (p != null)
								p.destroy();
						}
					}
				}
			}
		} catch (IOException e) {
		}
		return procInfos;
	}

	public IProcessInfo[] parseListTasks(InputStreamReader reader) {
		BufferedReader br = new BufferedReader(reader);
		ArrayList processList = new ArrayList();
		try {
			String line;
			while ((line = br.readLine()) != null) {
				int tab = line.indexOf('\t');
				if (tab != -1) {
					String proc = line.substring(0, tab).trim();
					String name = line.substring(tab).trim();
					if (proc.length() > 0 && name.length() > 0) {
						try {
							int pid = Integer.parseInt(proc);
							processList.add(new ProcessInfo(pid, name));
						} catch (NumberFormatException e) {
						}
					}
				}
			}
		} catch (IOException e) {
		}
		return (IProcessInfo[]) processList.toArray(new IProcessInfo[processList.size()]);
	}
}
