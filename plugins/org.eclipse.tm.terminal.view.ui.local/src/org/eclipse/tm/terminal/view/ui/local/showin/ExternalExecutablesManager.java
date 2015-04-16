/*******************************************************************************
 * Copyright (c) 2014, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.tm.terminal.view.ui.local.activator.UIPlugin;

/**
 * External executables manager implementation.
 */
public class ExternalExecutablesManager {

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

		return l;
	}

	/**
	 * Saves the list of external executables.
	 *
	 * @param l The list of external executables or <code>null</code>.
	 */
	public static void save(List<Map<String, String>> l) {
		IPath stateLocation = UIPlugin.getDefault().getStateLocation();
		if (stateLocation != null) {
			File f = stateLocation.append(".executables/data.properties").toFile(); //$NON-NLS-1$
			if (f.isFile() && (l == null || l.isEmpty())) {
				@SuppressWarnings("unused")
                boolean s = f.delete();
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
					} else if (id.height != 16 && d.height < id.height && id.width != 16 && d.width < id.width) {
						id = d;
					}
				}
			}
		}

		return id;
	}
}
