/*******************************************************************************
 * Copyright (c) 2016, 2018 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 		Red Hat Inc. - modified for use with Meson build
 *******************************************************************************/
package org.eclipse.cdt.internal.meson.core;

import java.nio.file.Files;
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

import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.meson.core.Activator;
import org.eclipse.cdt.meson.core.IMesonToolChainFile;
import org.eclipse.cdt.meson.core.IMesonToolChainListener;
import org.eclipse.cdt.meson.core.IMesonToolChainManager;
import org.eclipse.cdt.meson.core.IMesonToolChainProvider;
import org.eclipse.cdt.meson.core.MesonToolChainEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class MesonToolChainManager implements IMesonToolChainManager {

	private Map<Path, IMesonToolChainFile> files;

	private static final String N = "n"; //$NON-NLS-1$
	private static final String PATH = "__path"; //$NON-NLS-1$

	private final List<IMesonToolChainListener> listeners = new LinkedList<>();

	private Preferences getPreferences() {
		return InstanceScope.INSTANCE.getNode(Activator.getPluginId()).node("mesonToolchains"); //$NON-NLS-1$
	}

	private void init() {
		if (files == null) {
			files = new HashMap<>();

			Preferences prefs = getPreferences();
			try {
				for (String childName : prefs.childrenNames()) {
					Preferences tcNode = prefs.node(childName);
					String pathStr = tcNode.get(PATH, "/"); //$NON-NLS-1$
					Path path = Paths.get(pathStr);
					if (Files.exists(path) && !files.containsKey(path)) {
						IMesonToolChainFile file = new MesonToolChainFile(childName, path);
						for (String key : tcNode.keys()) {
							String value = tcNode.get(key, ""); //$NON-NLS-1$
							if (!value.isEmpty()) {
								file.setProperty(key, value);
							}
						}
						files.put(path, file);
					} else {
						tcNode.removeNode();
						prefs.flush();
					}
				}
			} catch (BackingStoreException e) {
				Activator.log(e);
			}

			// TODO discovery
			IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(Activator.getPluginId(),
					"toolChainProvider"); //$NON-NLS-1$
			for (IConfigurationElement element : point.getConfigurationElements()) {
				if (element.getName().equals("provider")) { //$NON-NLS-1$
					try {
						IMesonToolChainProvider provider = (IMesonToolChainProvider) element
								.createExecutableExtension("class"); //$NON-NLS-1$
						provider.init(this);
					} catch (ClassCastException | CoreException e) {
						Activator.log(e);
					}
				}
			}
		}
	}

	@Override
	public IMesonToolChainFile newToolChainFile(Path path) {
		return new MesonToolChainFile(null, path);
	}

	@Override
	public void addToolChainFile(IMesonToolChainFile file) {
		init();
		if (files.containsKey(file.getPath())) {
			removeToolChainFile(file);
		}
		files.put(file.getPath(), file);

		// save it

		MesonToolChainFile realFile = (MesonToolChainFile) file;
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

		fireEvent(new MesonToolChainEvent(MesonToolChainEvent.ADDED, file));
	}

	@Override
	public void removeToolChainFile(IMesonToolChainFile file) {
		init();
		fireEvent(new MesonToolChainEvent(MesonToolChainEvent.REMOVED, file));
		files.remove(file.getPath());

		String n = ((MesonToolChainFile) file).n;
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
	public IMesonToolChainFile getToolChainFile(Path path) {
		init();
		return files.get(path);
	}

	@Override
	public Collection<IMesonToolChainFile> getToolChainFiles() {
		init();
		return Collections.unmodifiableCollection(files.values());
	}

	@Override
	public Collection<IMesonToolChainFile> getToolChainFilesMatching(Map<String, String> properties) {
		List<IMesonToolChainFile> matches = new ArrayList<>();
		for (IMesonToolChainFile file : getToolChainFiles()) {
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

	@Override
	public IMesonToolChainFile getToolChainFileFor(IToolChain toolchain) {
		String id = toolchain.getId();

		for (IMesonToolChainFile file : getToolChainFiles()) {
			if (id.equals(file.getProperty(ICBuildConfiguration.TOOLCHAIN_ID))) {
				return file;
			}
		}

		return null;
	}

	@Override
	public void addListener(IMesonToolChainListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(IMesonToolChainListener listener) {
		listeners.remove(listener);
	}

	private void fireEvent(MesonToolChainEvent event) {
		for (IMesonToolChainListener listener : listeners) {
			SafeRunner.run(new ISafeRunnable() {
				@Override
				public void run() {
					listener.handleMesonToolChainEvent(event);
				}

				@Override
				public void handleException(Throwable exception) {
					Activator.log(exception);
				}
			});
		}
	}

}
