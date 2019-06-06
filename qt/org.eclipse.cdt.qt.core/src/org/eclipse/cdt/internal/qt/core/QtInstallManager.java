/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.core;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.qt.core.IQtInstall;
import org.eclipse.cdt.qt.core.IQtInstallListener;
import org.eclipse.cdt.qt.core.IQtInstallManager;
import org.eclipse.cdt.qt.core.IQtInstallProvider;
import org.eclipse.cdt.qt.core.QtInstallEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class QtInstallManager implements IQtInstallManager {

	private Map<Path, IQtInstall> installs;
	private Map<String, IConfigurationElement> toolChainMap;
	private List<IQtInstallListener> listeners = new LinkedList<>();

	private Preferences getPreferences() {
		return InstanceScope.INSTANCE.getNode(Activator.ID).node("qtInstalls"); //$NON-NLS-1$
	}

	private void initInstalls() {
		if (installs == null) {
			installs = new HashMap<>();
			try {
				Preferences prefs = getPreferences();
				for (String key : prefs.keys()) {
					QtInstall install = new QtInstall(Paths.get(prefs.get(key, "/"))); //$NON-NLS-1$
					installs.put(install.getQmakePath(), install);
				}
			} catch (BackingStoreException e) {
				Activator.log(e);
			}

			// Auto installs
			IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(Activator.ID,
					"qtInstallProvider"); //$NON-NLS-1$
			for (IConfigurationElement element : point.getConfigurationElements()) {
				try {
					IQtInstallProvider provider = (IQtInstallProvider) element.createExecutableExtension("class"); //$NON-NLS-1$
					for (IQtInstall install : provider.getInstalls()) {
						installs.put(install.getQmakePath(), install);
					}
				} catch (CoreException e) {
					Activator.log(e);
				}
			}
		}
	}

	private void saveInstalls() {
		try {
			Preferences prefs = getPreferences();

			// Remove ones that aren't valid
			for (String key : prefs.keys()) {
				if (installs.get(Paths.get(key)) == null) {
					prefs.remove(key);
				}
			}

			// Add new ones
			for (Path path : installs.keySet()) {
				String key = path.toString();
				if (prefs.get(key, null) == null) {
					prefs.put(key, installs.get(path).getQmakePath().toString());
				}
			}

			prefs.flush();
		} catch (BackingStoreException e) {
			Activator.log(e);
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
		installs.put(qt.getQmakePath(), qt);
		saveInstalls();
	}

	@Override
	public IQtInstall getInstall(Path qmakePath) {
		initInstalls();
		return installs.get(qmakePath);
	}

	@Override
	public Collection<IQtInstall> getInstall(String spec) {
		initInstalls();
		List<IQtInstall> installList = new ArrayList<>();
		for (IQtInstall install : installs.values()) {
			if (install.getSpec().equals(spec)) {
				installList.add(install);
			}
		}
		return installList;
	}

	@Override
	public void removeInstall(IQtInstall install) {
		fireEvent(new QtInstallEvent(QtInstallEvent.REMOVED, install));
		installs.remove(install.getQmakePath());
		saveInstalls();
	}

	@Override
	public boolean supports(IQtInstall install, IToolChain toolChain) {
		if (toolChainMap == null) {
			toolChainMap = new HashMap<>();
			IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(Activator.ID,
					"qtToolChainMapper"); //$NON-NLS-1$
			for (IConfigurationElement element : point.getConfigurationElements()) {
				if (element.getName().equals("mapping")) { //$NON-NLS-1$
					String spec = element.getAttribute("spec"); //$NON-NLS-1$
					toolChainMap.put(spec, element);
				}
			}
		}

		IConfigurationElement element = toolChainMap.get(install.getSpec());
		if (element != null) {
			for (IConfigurationElement property : element.getChildren("property")) { //$NON-NLS-1$
				String key = property.getAttribute("key"); //$NON-NLS-1$
				String value = property.getAttribute("value"); //$NON-NLS-1$
				if (!value.equals(toolChain.getProperty(key))) {
					return false;
				}
			}

			for (Entry<String, String> property : install.getProperties().entrySet()) {
				String tcValue = toolChain.getProperty(property.getKey());
				// Treat null as a wildcard
				if (tcValue != null && !property.getValue().equals(tcValue)) {
					return false;
				}
			}

			return true;
		} else {
			// Don't know so returning false
			return false;
		}
	}

	@Override
	public void addListener(IQtInstallListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(IQtInstallListener listener) {
		listeners.remove(listener);
	}

	private void fireEvent(QtInstallEvent event) {
		for (IQtInstallListener listener : listeners) {
			listener.installChanged(event);
		}
	}

}
