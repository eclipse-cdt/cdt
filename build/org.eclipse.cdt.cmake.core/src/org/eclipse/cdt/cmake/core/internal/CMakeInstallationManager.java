/*******************************************************************************
 * Copyright (c) 2017 Institute for Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.cdt.cmake.core.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.cdt.cmake.core.CMakeInstallation;
import org.eclipse.cdt.cmake.core.ICMakeInstallation;
import org.eclipse.cdt.cmake.core.ICMakeInstallation.Type;
import org.eclipse.cdt.cmake.core.ICMakeInstallationManager;
import org.eclipse.cdt.cmake.core.ICMakeInstallationProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class CMakeInstallationManager implements ICMakeInstallationManager {

	private static final String PREFERENCE_KEY_ACTIVE = "active";
	private static final String PREFERENCE_KEY_PATH = "path";
	private static final String PREFERENCE_KEY_TYPE = "type";
	
	private static final String PREFERENCE_NODE_ROOT = "cmakeInstallations";
	private static final String PREFERENCE_NODE_INSTALLATIONS = "installations";
			
	private Map<Path, ICMakeInstallation> installations = null;
	private ICMakeInstallation activeInstallation;
	
	private Preferences getPreferences() {
		return InstanceScope.INSTANCE.getNode(Activator.getId()).node(PREFERENCE_NODE_ROOT);
	}
	
	private void init() {
		if(installations != null && !installations.isEmpty()) {
			return;
		}
		
		installations = new TreeMap<>();
		
		loadFromPreferences();
		loadFromExtensions();
		loadActiveInstallation();
	}

	private void loadFromPreferences() {
		Preferences preferences = getPreferences().node(PREFERENCE_NODE_INSTALLATIONS);
		String[] children = null;
		
		try {
			children = preferences.childrenNames();
		} catch (BackingStoreException e) {
			Activator.log(e);
			return;
		}
		
		for (String child : children) {
			Preferences entry = preferences.node(child);
			Path path = Paths.get(entry.get(PREFERENCE_KEY_PATH, ""));
			
			try {
				Type type = Type.valueOf(entry.get(PREFERENCE_KEY_TYPE, "INVALID"));
				if (Files.exists(path) && Files.isDirectory(path)) {
					installations.put(path, new CMakeInstallation(path, type));
				} else {
					removeFromPreferences(entry);
				}
			} catch (IllegalArgumentException | IOException e) {
				Activator.log(e);
				removeFromPreferences(entry);
			}
		}

		savePreferences();
	}

	private void loadFromExtensions() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(Activator.getId(), "installationProvider");
		IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
		try {
			for (IConfigurationElement element : elements) {
				if (element.getName().equals("provider")) {
					ICMakeInstallationProvider provider = (ICMakeInstallationProvider) element
							.createExecutableExtension("class");
					provider.init();
					Collection<ICMakeInstallation> extensionInstallations = provider.getInstallations();
					for(ICMakeInstallation extensionInstallation : extensionInstallations) {
						if(!installations.containsKey(extensionInstallation.getRoot())) {
							storeInstallation(extensionInstallation);
						}
					}
				}
			}
		} catch (CoreException e) {
			Activator.log(e);
		}
	}

	private void loadActiveInstallation() {
		Preferences preferences = getPreferences();
		String active = preferences.get(PREFERENCE_KEY_ACTIVE, "");
		if(active.isEmpty()) {
			if(!installations.isEmpty()) {
				setActive(getInstallations().get(0));
			}
		} else {
			ICMakeInstallation installation = installations.get(Paths.get(active));
			if(installation != null) {
				setActive(installation);
			} else if(!installations.isEmpty()) {
				setActive(getInstallations().get(0));
			}
		}
	}

	private void storeInstallation(ICMakeInstallation installation) {
		installations.put(installation.getRoot(), installation);
		Preferences entry = getPreferences().node(PREFERENCE_NODE_INSTALLATIONS).node(UUID.randomUUID().toString());
		entry.put(PREFERENCE_KEY_PATH, installation.getRoot().toString());
		entry.put(PREFERENCE_KEY_TYPE, installation.getType().name());
		savePreferences();
	}
	
	private void savePreferences() {
		try {
			getPreferences().flush();
		} catch (BackingStoreException e) {
			Activator.log(e);
		}
	}
	
	private void removeFromPreferences(Preferences entry) {
		try {
			entry.removeNode();
		} catch (BackingStoreException e) {
			Activator.log(e);
		}
	}

	@Override
	public void add(ICMakeInstallation installation) {
		init();
		if(!installations.containsKey(installation.getRoot())) {
				storeInstallation(installation);
		}
	}

	@Override
	public void remove(ICMakeInstallation installation) {
		init();
		installations.remove(installation.getRoot());
		if(installation == activeInstallation) {
			if(installations.isEmpty()) {
				activeInstallation = null;
			} else {
				activeInstallation = getInstallations().get(0);
			}
		}
	}

	@Override
	public List<ICMakeInstallation> getInstallations() {
		init();
		List<ICMakeInstallation> list = installations.values().stream().collect(Collectors.toList());
		Collections.sort(list);
		return list;
	}

	@Override
	public ICMakeInstallation getActive() {
		init();
		return activeInstallation;
	}

	@Override
	public void setActive(ICMakeInstallation installation) {
		init();
		Preferences preferences = getPreferences();
		preferences.put(PREFERENCE_KEY_ACTIVE, installation.getRoot().toString());
		activeInstallation = installation;
		savePreferences();
	}

}
