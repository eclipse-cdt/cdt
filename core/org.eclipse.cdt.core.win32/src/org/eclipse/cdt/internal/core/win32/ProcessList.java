/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.win32;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IProcessInfo;
import org.eclipse.cdt.core.IProcessList;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
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

	public IProcessInfo[] getProcessList() {
		Process p = null;
		String command = null;
		InputStream in = null;
		Bundle bundle = Platform.getBundle(CCorePlugin.PLUGIN_ID);

		try {
			URL url = Platform.find(bundle, new Path("$os$/listtasks.exe")); //$NON-NLS-1$
			url = Platform.resolve(url);
			String path = url.getFile();
			File file = new File(path);
			if (file.exists()) {
				command = file.getCanonicalPath();
				if (command != null) {
					p = ProcessFactory.getFactory().exec(command);
					in = p.getInputStream();
					InputStreamReader reader = new InputStreamReader(in);
					return parseListTasks(reader);
				}
			}
		} catch (IOException e) {
		}
		return NOPROCESS;
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
