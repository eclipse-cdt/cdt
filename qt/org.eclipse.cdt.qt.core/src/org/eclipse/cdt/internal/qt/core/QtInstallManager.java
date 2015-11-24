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

import org.eclipse.cdt.build.core.IToolChain;
import org.eclipse.cdt.qt.core.IQtInstall;
import org.eclipse.cdt.qt.core.IQtInstallManager;
import org.eclipse.cdt.qt.core.IQtInstallTargetMapper;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class QtInstallManager implements IQtInstallManager {

	private Map<String, IQtInstall> installs;
	private Map<String, IConfigurationElement> mapperElements;
	private Map<String, IQtInstallTargetMapper> mappers;

	private Preferences getPreferences() {
		return ConfigurationScope.INSTANCE.getNode(Activator.ID).node("qtInstalls"); //$NON-NLS-1$
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
				Activator.log(e);
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

	@Override
	public boolean supports(IQtInstall install, ILaunchTarget target) {
		if (mapperElements == null) {
			// init the extension point
			mapperElements = new HashMap<>();
			mappers = new HashMap<>();

			IExtensionPoint point = Platform.getExtensionRegistry()
					.getExtensionPoint(Activator.ID + ".qtInstallTargetMapper"); //$NON-NLS-1$
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement element : extension.getConfigurationElements()) {
					String targetTypeId = element.getAttribute("targetTypeId"); //$NON-NLS-1$
					mapperElements.put(targetTypeId, element);
				}
			}
		}

		String targetTypeId = target.getTypeId();
		IQtInstallTargetMapper mapper = mappers.get(targetTypeId);
		if (mapper == null) {
			IConfigurationElement element = mapperElements.get(targetTypeId);
			if (element != null) {
				try {
					mapper = (IQtInstallTargetMapper) element.createExecutableExtension("class"); //$NON-NLS-1$
					mappers.put(targetTypeId, mapper);
				} catch (CoreException e) {
					Activator.log(e);
				}
			}
		}

		if (mapper == null) {
			return false;
		}

		return mapper.supported(install, target);
	}

	@Override
	public boolean supports(IQtInstall install, IToolChain toolChain) {
		// TODO need another extension point for this
		return true;
	}

}
