/*******************************************************************************
 * Copyright (c) 2014, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.local.showin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Platform;
import org.eclipse.tm.terminal.connector.local.showin.interfaces.IExternalExecutablesProperties;
import org.eclipse.ui.IStartup;

/**
 * External executables data initializer.
 */
public class ExternalExecutablesInitializer implements IStartup {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IStartup#earlyStartup()
	 */
	@Override
	public void earlyStartup() {
		// On Windows, initialize the "Git Bash" custom "Show In" menu entry
		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			String gitPath = null;
			String iconPath = null;

			String path = System.getenv("PATH"); //$NON-NLS-1$
			if (path != null) {
				StringTokenizer tokenizer = new StringTokenizer(path, ";"); //$NON-NLS-1$
				while (tokenizer.hasMoreTokens()) {
					String token = tokenizer.nextToken();
					File f = new File(token, "git.exe"); //$NON-NLS-1$
					if (f.canRead()) {
						File f2 = new File(f.getParentFile().getParentFile(), "bin/sh.exe"); //$NON-NLS-1$
						if (f2.canExecute()) {
							gitPath = f2.getAbsolutePath();
						}

						f2 = new File(f.getParentFile().getParentFile(), "etc/git.ico"); //$NON-NLS-1$
						if (f2.canRead()) {
							iconPath = f2.getAbsolutePath();
						}

						break;
					}
				}
			}

			if (gitPath != null) {
				// Load the configured external executables
				List<Map<String, String>> l = ExternalExecutablesManager.load();
				if (l == null) l = new ArrayList<Map<String, String>>();
				// Find a entry labeled "Git Bash"
				Map<String, String> m = null;
				for (Map<String, String> candidate : l) {
					String name = candidate.get(IExternalExecutablesProperties.PROP_NAME);
					if ("Git Bash".equals(name)) { //$NON-NLS-1$
						m = candidate;
						break;
					}
				}

				if (m == null) {
					m = new HashMap<String, String>();
					m.put(IExternalExecutablesProperties.PROP_NAME, "Git Bash"); //$NON-NLS-1$
					m.put(IExternalExecutablesProperties.PROP_PATH, gitPath);
					m.put(IExternalExecutablesProperties.PROP_ARGS, "--login -i"); //$NON-NLS-1$
					if (iconPath != null) m.put(IExternalExecutablesProperties.PROP_ICON, iconPath);
					m.put(IExternalExecutablesProperties.PROP_TRANSLATE, Boolean.TRUE.toString());

					l.add(m);
					ExternalExecutablesManager.save(l);
				}
			}
		}
	}
}
