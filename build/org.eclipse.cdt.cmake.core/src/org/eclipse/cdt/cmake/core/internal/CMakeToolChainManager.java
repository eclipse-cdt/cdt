/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.cmake.core.internal;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.cmake.core.ICMakeToolChainFile;
import org.eclipse.cdt.cmake.core.ICMakeToolChainManager;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class CMakeToolChainManager implements ICMakeToolChainManager {

	private Map<Path, ICMakeToolChainFile> files;

	private static final String N = "n"; //$NON-NLS-1$
	private static final String PATH = "__path"; //$NON-NLS-1$

	private Preferences getPreferences() {
		return InstanceScope.INSTANCE.getNode(Activator.getId()).node("cmakeToolchains"); //$NON-NLS-1$
	}

	private void init() {
		if (files == null) {
			files = new HashMap<>();

			Preferences prefs = getPreferences();
			try {
				for (String childName : prefs.childrenNames()) {
					Preferences tcNode = prefs.node(childName);
					String path = tcNode.get(PATH, "/"); //$NON-NLS-1$
					ICMakeToolChainFile file = new CMakeToolChainFile(childName, Paths.get(path));
					for (String key : tcNode.keys()) {
						String value = tcNode.get(key, ""); //$NON-NLS-1$
						if (!value.isEmpty()) {
							file.setProperty(key, value);
						}
					}
					files.put(file.getPath(), file);
				}
			} catch (BackingStoreException e) {
				Activator.log(e);
			}

			// TODO discovery
		}
	}

	@Override
	public ICMakeToolChainFile newToolChainFile(Path path) {
		return new CMakeToolChainFile(null, path);
	}

	@Override
	public void addToolChainFile(ICMakeToolChainFile file) {
		init();
		files.put(file.getPath(), file);

		// save it

		CMakeToolChainFile realFile = (CMakeToolChainFile) file;
		Preferences prefs = getPreferences();
		String n = realFile.n;
		if (n == null) {
			n = prefs.get(N, "0"); //$NON-NLS-1$
			realFile.n = n;
		}
		prefs.put(N, Integer.toString(Integer.parseInt(n) + 1));

		Preferences tcNode = prefs.node(n);
		tcNode.put(PATH, file.getPath().toString());
		for (Entry<String, String> entry : realFile.properties.entrySet()) {
			tcNode.put(entry.getKey(), entry.getValue());
		}

		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			Activator.log(e);
		}
	}

	@Override
	public void removeToolChainFile(ICMakeToolChainFile file) {
		init();
		files.remove(file.getPath());

		String n = ((CMakeToolChainFile) file).n;
		if (n != null) {
			Preferences prefs = getPreferences();
			Preferences tcNode = prefs.node(n);
			try {
				tcNode.removeNode();
				prefs.flush();
			} catch (BackingStoreException e) {
				Activator.log(e);
			}
		}
	}

	@Override
	public ICMakeToolChainFile getToolChainFile(Path path) {
		init();
		return files.get(path);
	}

	@Override
	public Collection<ICMakeToolChainFile> getToolChainFiles() {
		init();
		return Collections.unmodifiableCollection(files.values());
	}

	@Override
	public Collection<ICMakeToolChainFile> getToolChainsFileMatching(Map<String, String> properties) {
		List<ICMakeToolChainFile> matches = new ArrayList<>();
		for (ICMakeToolChainFile file : getToolChainFiles()) {
			boolean match = true;
			for (Entry<String, String> entry : properties.entrySet()) {
				if (!entry.getValue().equals(file.getProperty(entry.getKey()))) {
					match = false;
					break;
				}
			}

			if (match) {
				matches.add(file);
			}
		}
		return matches;
	}

}
