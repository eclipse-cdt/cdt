/*******************************************************************************
 * Copyright (c) 2007, 2016 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

/**
 * Abstract image registry that allows for defining fallback paths for images.
 */
public abstract class AbstractImageRegistry {
	private HashMap<String, String> fPlugins = new HashMap<>();
	private HashMap<String, String[]> fLocations = new HashMap<>();
	private URL fBaseUrl;
	private final ImageRegistry fRegistry;

	protected AbstractImageRegistry(Plugin plugin) {
		fRegistry = new ImageRegistry();
		fBaseUrl = plugin.getBundle().getEntry("/"); //$NON-NLS-1$
	}

	/**
	 * Defines the key for a local image, that must be found below the icons directory
	 * in the plugin.
	 * @param key Key by which the image can be referred by.
	 * @param dir Directory relative to icons/
	 * @param name The name of the file defining the icon. The name will be used as
	 *   key.
	 */
	protected void localImage(String key, String dir, String name) {
		if (dir == null || dir.isEmpty())
			fLocations.put(key, new String[] { "icons/" + name }); //$NON-NLS-1$
		else
			fLocations.put(key, new String[] { "icons/" + dir + "/" + name }); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Defines the key for a non-local image, that must be found below the icons directory
	 * of some plugin.
	 * @param key Key by which the image can be referred by.
	 * @param plugin The plugin id, where the icon is searched.
	 * @param dirs A couple of directories below icons/ in the plugin. If loading fails,
	 * the next dir will be taken as fallback.
	 * @param name The name of the file defining the icon. The name will be used as
	 *   key.
	 */
	protected void externalImage(String key, String plugin, String[] dirs, String name) {
		if (plugin != null) {
			fPlugins.put(key, plugin);
		}
		String[] locations = new String[dirs.length];
		for (int i = 0; i < dirs.length; i++) {
			String dir = dirs[i];
			if (dir == null || dir.isEmpty())
				locations[i] = "icons/" + name; //$NON-NLS-1$
			else
				locations[i] = "icons/" + dir + "/" + name; //$NON-NLS-1$ //$NON-NLS-2$
		}
		fLocations.put(key, locations);
	}

	final public Image get(String key) {
		Image i = fRegistry.get(key);
		if (i != null) {
			return i;
		}

		ImageDescriptor d = createFileImageDescriptor(key);
		if (d != null) {
			fRegistry.put(key, d);
			return fRegistry.get(key);
		}
		return null;
	}

	final public ImageDescriptor getDescriptor(String key) {
		ImageDescriptor d = fRegistry.getDescriptor(key);
		if (d != null) {
			return d;
		}

		d = createFileImageDescriptor(key);
		if (d != null) {
			fRegistry.put(key, d);
			return d;
		}
		return null;
	}

	private ImageDescriptor createFileImageDescriptor(String key) {
		URL url = fBaseUrl;
		String pluginId = fPlugins.get(key);
		if (pluginId != null) {
			Bundle bundle = Platform.getBundle(pluginId);
			if (bundle != null) {
				url = bundle.getEntry("/"); //$NON-NLS-1$
			}
		}
		String[] locations = fLocations.get(key);
		if (locations != null) {
			for (int i = 0; i < locations.length; i++) {
				String loc = locations[i];
				URL full;
				try {
					full = new URL(url, loc);
					ImageDescriptor candidate = ImageDescriptor.createFromURL(full);
					if (candidate != null && candidate.getImageData() != null) {
						return candidate;
					}
				} catch (MalformedURLException e) {
					DsfUIPlugin.getDefault().getLog()
							.log(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, "Malformed Icon URL", e)); //$NON-NLS-1$
				} catch (SWTException e) {
					// try the next one.
				}
			}
		}
		return null;
	}

}
