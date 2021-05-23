/*******************************************************************************
 * Copyright (c) 2014, 2021 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * Dirk Fauth <dirk.fauth@googlemail.com> - Bug 460496
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.local.showin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.tm.terminal.view.ui.activator.UIPlugin;
import org.eclipse.tm.terminal.view.ui.internal.ExternalExecutablesState;
import org.eclipse.tm.terminal.view.ui.local.showin.detectors.DetectGitBash;
import org.eclipse.tm.terminal.view.ui.local.showin.detectors.DetectWSL;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.ISourceProviderService;

/**
 * External executables manager implementation.
 */
public class ExternalExecutablesManager {
	// XXX: This may make a useful extension point?
	private static List<IDetectExternalExecutable> detectors = List.of(new DetectGitBash(), new DetectWSL());

	public static boolean hasEntries() {
		IPath stateLocation = UIPlugin.getDefault().getStateLocation();
		if (stateLocation != null) {
			File f = stateLocation.append(".executables/data.properties").toFile(); //$NON-NLS-1$
			if (f.canRead()) {

				try (FileReader r = new FileReader(f)) {
					Properties data = new Properties();
					data.load(r);
					if (!data.isEmpty()) {
						return true;
					}
				} catch (IOException e) {
					if (Platform.inDebugMode()) {
						e.printStackTrace();
					}
				}
			}
		}

		// There are not any saved entries - run the detectors and if any of them
		// have entries, then we can stop.
		for (IDetectExternalExecutable iDetectExternalExecutable : detectors) {
			if (iDetectExternalExecutable.hasEntries()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Loads the list of all saved external executables.
	 *
	 * @return The list of all saved external executables or <code>null</code>.
	 */
	public static List<Map<String, String>> load() {
		List<Map<String, String>> externalExecutables = new ArrayList<>();

		IPath stateLocation = UIPlugin.getDefault().getStateLocation();
		if (stateLocation != null) {
			File f = stateLocation.append(".executables/data.properties").toFile(); //$NON-NLS-1$
			if (f.canRead()) {
				FileReader r = null;

				try {
					Properties data = new Properties();
					r = new FileReader(f);
					data.load(r);

					Map<Integer, Map<String, String>> c = new HashMap<>();
					for (String name : data.stringPropertyNames()) {
						if (name == null || name.indexOf('.') == -1)
							continue;
						int ix = name.indexOf('.');
						String n = name.substring(0, ix);
						String k = (ix + 1) < name.length() ? name.substring(ix + 1) : null;
						if (n == null || k == null)
							continue;

						Integer i = null;
						try {
							i = Integer.decode(n);
						} catch (NumberFormatException e) {
							/* ignored on purpose */ }
						if (i == null)
							continue;

						Map<String, String> m = c.get(i);
						if (m == null) {
							m = new HashMap<>();
							c.put(i, m);
						}
						Assert.isNotNull(m);

						m.put(k, data.getProperty(name));
					}

					List<Integer> k = new ArrayList<>(c.keySet());
					Collections.sort(k);
					for (Integer i : k) {
						Map<String, String> m = c.get(i);
						if (m != null && !m.isEmpty())
							externalExecutables.add(m);
					}
				} catch (Exception e) {
					if (Platform.inDebugMode()) {
						e.printStackTrace();
					}
				} finally {
					if (r != null)
						try {
							r.close();
						} catch (IOException e) {
							/* ignored on purpose */
						}
				}
			}
		}

		var readOnly = Collections.unmodifiableList(externalExecutables);
		var detected = detectors.stream().flatMap(detector -> detector.getEntries(readOnly).stream())
				.collect(Collectors.toList());
		if (!detected.isEmpty()) {
			externalExecutables.addAll(detected);
			save(externalExecutables);
		}

		return externalExecutables;
	}

	/**
	 * Saves the list of external executables.
	 *
	 * @param externalExecutables The list of external executables or <code>null</code>.
	 */
	public static void save(List<Map<String, String>> externalExecutables) {
		ISourceProviderService sourceProviderService = PlatformUI.getWorkbench()
				.getService(ISourceProviderService.class);
		ExternalExecutablesState stateService = (ExternalExecutablesState) sourceProviderService
				.getSourceProvider(ExternalExecutablesState.CONFIGURED_STATE);

		IPath stateLocation = UIPlugin.getDefault().getStateLocation();
		if (stateLocation != null) {
			File f = stateLocation.append(".executables/data.properties").toFile(); //$NON-NLS-1$
			if (f.isFile() && (externalExecutables == null || externalExecutables.isEmpty())) {
				@SuppressWarnings("unused")
				boolean s = f.delete();

				if (stateService != null)
					stateService.disable();
			} else {
				FileWriter w = null;

				try {
					Properties data = new Properties();
					for (int i = 0; i < externalExecutables.size(); i++) {
						Map<String, String> m = externalExecutables.get(i);
						for (Entry<String, String> e : m.entrySet()) {
							String key = Integer.toString(i) + "." + e.getKey(); //$NON-NLS-1$
							data.setProperty(key, e.getValue());
						}
					}

					if (!f.exists()) {
						@SuppressWarnings("unused")
						boolean s = f.getParentFile().mkdirs();
						s = f.createNewFile();
					}
					w = new FileWriter(f);
					data.store(w, null);

					if (stateService != null)
						stateService.enable();
				} catch (Exception e) {
					if (Platform.inDebugMode()) {
						e.printStackTrace();
					}
				} finally {
					if (w != null) {
						try {
							w.flush();
							w.close();
						} catch (IOException e) {
							/* ignored on purpose */
						}
					}
				}
			}
		}
	}
}
