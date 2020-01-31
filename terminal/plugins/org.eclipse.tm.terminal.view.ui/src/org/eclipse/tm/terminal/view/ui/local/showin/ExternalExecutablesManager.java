/*******************************************************************************
 * Copyright (c) 2014, 2018 Wind River Systems, Inc. and others. All rights reserved.
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
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.tm.terminal.view.ui.activator.UIPlugin;
import org.eclipse.tm.terminal.view.ui.interfaces.IExternalExecutablesProperties;
import org.eclipse.tm.terminal.view.ui.internal.ExternalExecutablesState;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.ISourceProviderService;

/**
 * External executables manager implementation.
 */
public class ExternalExecutablesManager {
	// Flag to indicate if we have searched for git bash already
	private static boolean gitBashSearchDone = false;

	/**
	 * Loads the list of all saved external executables.
	 *
	 * @return The list of all saved external executables or <code>null</code>.
	 */
	public static List<Map<String, String>> load() {
		List<Map<String, String>> l = new ArrayList<Map<String, String>>();

		IPath stateLocation = UIPlugin.getDefault().getStateLocation();
		if (stateLocation != null) {
			File f = stateLocation.append(".executables/data.properties").toFile(); //$NON-NLS-1$
			if (f.canRead()) {
				FileReader r = null;

				try {
					Properties data = new Properties();
					r= new FileReader(f);
					data.load(r);

					Map<Integer, Map<String, String>> c = new HashMap<Integer, Map<String, String>>();
					for (String name : data.stringPropertyNames()) {
						if (name == null || name.indexOf('.') == -1) continue;
						int ix = name.indexOf('.');
						String n = name.substring(0, ix);
						String k = (ix + 1) < name.length() ? name.substring(ix + 1) : null;
						if (n == null || k == null) continue;

						Integer i = null;
						try { i = Integer.decode(n); } catch (NumberFormatException e) { /* ignored on purpose */ }
						if (i == null) continue;

						Map<String, String> m = c.get(i);
						if (m == null) {
							m = new HashMap<String, String>();
							c.put(i, m);
						}
						Assert.isNotNull(m);

						m.put(k, data.getProperty(name));
					}

					List<Integer> k = new ArrayList<Integer>(c.keySet());
					Collections.sort(k);
					for (Integer i : k) {
						Map<String, String> m = c.get(i);
						if (m != null && !m.isEmpty()) l.add(m);
					}
				} catch (Exception e) {
					if (Platform.inDebugMode()) {
						e.printStackTrace();
					}
				} finally {
					if (r != null) try { r.close(); } catch (IOException e) { /* ignored on purpose */ }
				}
			}
		}

		// Lookup git bash (Windows Hosts only)
		if (!gitBashSearchDone && Platform.OS_WIN32.equals(Platform.getOS())) {
			// Check the existing entries first
			// Find a entry labeled "Git Bash"
			Map<String, String> m = null;
			for (Map<String, String> candidate : l) {
				String name = candidate.get(IExternalExecutablesProperties.PROP_NAME);
				if ("Git Bash".equals(name)) { //$NON-NLS-1$
					m = candidate;
					break;
				}
			}

			// If not found in the existing entries, check the path
			if (m == null) {
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

							iconPath = getGitIconPath(f.getParentFile().getParentFile());

							break;
						}
					}
				}

				// if it is not found in the PATH, check the default install locations
				if (gitPath == null) {
					File f = new File("C:/Program Files (x86)/Git/bin/sh.exe"); //$NON-NLS-1$
					if (!f.exists()) {
						f = new File("C:/Program Files/Git/bin/sh.exe"); //$NON-NLS-1$
					}

					if (f.exists() && f.canExecute()) {
						gitPath = f.getAbsolutePath();
						iconPath = getGitIconPath(f.getParentFile().getParentFile());
					}
				}

				if (gitPath != null) {
					m = new HashMap<String, String>();
					m.put(IExternalExecutablesProperties.PROP_NAME, "Git Bash"); //$NON-NLS-1$
					m.put(IExternalExecutablesProperties.PROP_PATH, gitPath);
					m.put(IExternalExecutablesProperties.PROP_ARGS, "--login -i"); //$NON-NLS-1$
					if (iconPath != null) m.put(IExternalExecutablesProperties.PROP_ICON, iconPath);
					m.put(IExternalExecutablesProperties.PROP_TRANSLATE, Boolean.TRUE.toString());

					l.add(m);
					save(l);
				}
			}

			// Do not search again for git bash while the session is running
			gitBashSearchDone = true;
		}

		return l;
	}

	private static String getGitIconPath(File parent) {
		File f = new File(parent, "etc/git.ico"); //$NON-NLS-1$
		if (f.canRead()) {
			return f.getAbsolutePath();
		}

		// check for icon in newer versions of Git for Windows 32 bit
		f = new File(parent, "mingw32/share/git/git-for-windows.ico"); //$NON-NLS-1$
		if (f.canRead()) {
			return f.getAbsolutePath();
		}

		// check for icon in newer versions of Git for Windows 64 bit
		f = new File(parent, "mingw64/share/git/git-for-windows.ico"); //$NON-NLS-1$
		if (f.canRead()) {
			return f.getAbsolutePath();
		}

		return null;
	}

	/**
	 * Saves the list of external executables.
	 *
	 * @param l The list of external executables or <code>null</code>.
	 */
	@SuppressWarnings("cast")
    public static void save(List<Map<String, String>> l) {
		ISourceProviderService sourceProviderService = (ISourceProviderService) PlatformUI.getWorkbench().getService(ISourceProviderService.class);
		ExternalExecutablesState stateService = (ExternalExecutablesState) sourceProviderService.getSourceProvider(ExternalExecutablesState.CONFIGURED_STATE);

		IPath stateLocation = UIPlugin.getDefault().getStateLocation();
		if (stateLocation != null) {
			File f = stateLocation.append(".executables/data.properties").toFile(); //$NON-NLS-1$
			if (f.isFile() && (l == null || l.isEmpty())) {
				@SuppressWarnings("unused")
                boolean s = f.delete();

				if (stateService != null) stateService.disable();
			} else {
				FileWriter w = null;

				try {
					Properties data = new Properties();
					for (int i = 0; i < l.size(); i++) {
						Map<String, String> m = l.get(i);
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

					if (stateService != null) stateService.enable();
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

	/**
	 * Loads the image data suitable for showing an icon in a menu
	 * (16 x 16, 8bit depth) from the given file.
	 *
	 * @param path The image file path. Must not be <code>null</code>.
	 * @return The image data or <code>null</code>.
	 */
	public static ImageData loadImage(String path) {
		Assert.isNotNull(path);

		ImageData id = null;
		ImageData biggest = null;

		ImageLoader loader = new ImageLoader();
		ImageData[] data = loader.load(path);

		if (data != null) {
			for (ImageData d : data) {
				if (d.height == 16 && d.width == 16) {
					if (id == null || id.height != 16 && id.width != 16) {
						id = d;
					} else if (d.depth < id.depth && d.depth >= 8){
						id = d;
					}
				} else {
					if (id == null) {
						id = d;
						biggest = d;
					} else if (id.height != 16 && d.height < id.height && id.width != 16 && d.width < id.width) {
						id = d;
					} else if (biggest == null || d.height > biggest.height && d.width > biggest.width) {
						biggest = d;
					}
				}
			}
		}

		// if the icon is still to big -> downscale the biggest
		if (id != null && id.height > 16 && id.width > 16) {
			id = biggest.scaledTo(16, 16);
		}

		return id;
	}
}
