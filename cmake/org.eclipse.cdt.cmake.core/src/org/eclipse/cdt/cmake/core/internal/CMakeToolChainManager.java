/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.core.internal;

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

import org.eclipse.cdt.cmake.core.CMakeToolChainEvent;
import org.eclipse.cdt.cmake.core.ICMakeToolChainFile;
import org.eclipse.cdt.cmake.core.ICMakeToolChainListener;
import org.eclipse.cdt.cmake.core.ICMakeToolChainManager;
import org.eclipse.cdt.cmake.core.ICMakeToolChainProvider;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class CMakeToolChainManager implements ICMakeToolChainManager {

	private Map<String, ICMakeToolChainFile> filesByToolChain;

	private static final String N = "n"; //$NON-NLS-1$
	private static final String PATH = "__path"; //$NON-NLS-1$

	private final List<ICMakeToolChainListener> listeners = new LinkedList<>();

	private Preferences getPreferences() {
		return InstanceScope.INSTANCE.getNode(Activator.getId()).node("cmakeToolchains"); //$NON-NLS-1$
	}

	public static String makeToolChainId(String tcType, String tcId) {
		return tcType + '/' + tcId;
	}

	public static String makeToolChainId(IToolChain toolchain) {
		return makeToolChainId(toolchain.getTypeId(), toolchain.getId());
	}

	private synchronized void init() {
		if (filesByToolChain == null) {
			filesByToolChain = new HashMap<>();

			Preferences prefs = getPreferences();
			try {
				for (String childName : prefs.childrenNames()) {
					Preferences tcNode = prefs.node(childName);
					String pathStr = tcNode.get(PATH, "/"); //$NON-NLS-1$
					String tcType = tcNode.get(CMakeBuildConfiguration.TOOLCHAIN_TYPE, "?"); //$NON-NLS-1$
					String tcId = tcNode.get(CMakeBuildConfiguration.TOOLCHAIN_ID, "?"); //$NON-NLS-1$
					Path path = Paths.get(pathStr);
					IToolChainManager tcManager = Activator.getService(IToolChainManager.class);
					IToolChain toolchain = tcManager.getToolChain(tcType, tcId);
					if (toolchain != null && Files.exists(path)) {
						ICMakeToolChainFile file = new CMakeToolChainFile(childName, path);
						for (String key : tcNode.keys()) {
							String value = tcNode.get(key, ""); //$NON-NLS-1$
							if (!value.isEmpty()) {
								file.setProperty(key, value);
							}
						}
						filesByToolChain.put(makeToolChainId(tcType, tcId), file);
					} else {
						tcNode.removeNode();
						prefs.flush();
					}
				}
			} catch (BackingStoreException | CoreException e) {
				Activator.log(e);
			}

			// TODO discovery
			IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(Activator.getId(),
					"toolChainProvider"); //$NON-NLS-1$
			for (IConfigurationElement element : point.getConfigurationElements()) {
				if (element.getName().equals("provider")) { //$NON-NLS-1$
					try {
						ICMakeToolChainProvider provider = (ICMakeToolChainProvider) element
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
	public ICMakeToolChainFile newToolChainFile(Path path) {
		return new CMakeToolChainFile(null, path);
	}

	@Override
	public void addToolChainFile(ICMakeToolChainFile file) {
		init();
		try {
			IToolChain toolchain = file.getToolChain();
			String tcId = makeToolChainId(toolchain);
			if (filesByToolChain.containsKey(tcId)) {
				removeToolChainFile(file);
			}
			filesByToolChain.put(tcId, file);

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
			tcNode.put(CMakeBuildConfiguration.TOOLCHAIN_TYPE, toolchain.getTypeId());
			tcNode.put(CMakeBuildConfiguration.TOOLCHAIN_ID, toolchain.getId());

			prefs.flush();

			fireEvent(new CMakeToolChainEvent(CMakeToolChainEvent.ADDED, file));
		} catch (CoreException | BackingStoreException e) {
			Activator.log(e);
			return;
		}
	}

	@Override
	public void removeToolChainFile(ICMakeToolChainFile file) {
		init();
		fireEvent(new CMakeToolChainEvent(CMakeToolChainEvent.REMOVED, file));
		String tcId = makeToolChainId(file.getProperty(CMakeBuildConfiguration.TOOLCHAIN_TYPE),
				file.getProperty(CMakeBuildConfiguration.TOOLCHAIN_ID));
		filesByToolChain.remove(tcId);

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
		return null;
	}

	@Override
	public Collection<ICMakeToolChainFile> getToolChainFiles() {
		init();
		return Collections.unmodifiableCollection(filesByToolChain.values());
	}

	@Override
	public Collection<ICMakeToolChainFile> getToolChainFilesMatching(Map<String, String> properties) {
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

	@Override
	public ICMakeToolChainFile getToolChainFileFor(IToolChain toolchain) {
		init();
		String id = makeToolChainId(toolchain);
		return filesByToolChain.get(id);
	}

	@Override
	public void addListener(ICMakeToolChainListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(ICMakeToolChainListener listener) {
		listeners.remove(listener);
	}

	private void fireEvent(CMakeToolChainEvent event) {
		for (ICMakeToolChainListener listener : listeners) {
			SafeRunner.run(new ISafeRunnable() {
				@Override
				public void run() throws Exception {
					listener.handleCMakeToolChainEvent(event);
				}

				@Override
				public void handleException(Throwable exception) {
					Activator.log(exception);
				}
			});
		}
	}

}
