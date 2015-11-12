/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.core;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.qt.core.IQtInstall;
import org.eclipse.cdt.qt.core.IQtInstallManager;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class QtInstallManager implements IQtInstallManager {

	private Map<String, IQtInstall> installs;

	private Preferences getPreferences() {
		return InstanceScope.INSTANCE.getNode(QtPlugin.ID).node("qtInstalls"); //$NON-NLS-1$
	}

	private void initInstalls() {
		if (installs == null) {
			installs = new HashMap<>();
			try {
				Preferences prefs = getPreferences();
				for (String key : prefs.keys()) {
					installs.put(key, new QtInstall(key, Paths.get(prefs.get(key, "/")))); //$NON-NLS-1$
				}
			} catch (BackingStoreException e) {
				QtPlugin.log(e);
			}
		}
	}

	private void saveInstalls() {
		try {
			Preferences prefs = getPreferences();

			// Remove ones that aren't valid
			for (String key : prefs.keys()) {
				if (installs.get(key) == null) {
					prefs.remove(key);
				}
			}

			// Add new ones
			for (String key : installs.keySet()) {
				if (prefs.get(key, null) == null) {
					prefs.put(key, installs.get(key).getQmakePath().toString());
				}
			}
		} catch (BackingStoreException e) {
			QtPlugin.log(e);
		}
	}

	@Override
	public Collection<IQtInstall> getInstalls() {
		initInstalls();
		return Collections.unmodifiableCollection(installs.values());
	}

	@Override
	public void addInstall(IQtInstall qt) {
		initInstalls();
		installs.put(qt.getName(), qt);
		saveInstalls();
	}

	@Override
	public IQtInstall getInstall(String name) {
		initInstalls();
		return installs.get(name);
	}

	@Override
	public void removeInstall(IQtInstall install) {
		installs.remove(install.getName());
		saveInstalls();
	}

}
