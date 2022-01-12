/*******************************************************************************
 * Copyright (c) 2010, 2012 Alena Laskavaia and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui;

import org.eclipse.cdt.codan.core.CodanCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class CodanUIActivator extends AbstractUIPlugin {
	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.codan.ui"; //$NON-NLS-1$
	// The shared instance
	private static CodanUIActivator plugin;
	private IPreferenceStore corePreferenceStore;

	/**
	 * The constructor
	 */
	public CodanUIActivator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static CodanUIActivator getDefault() {
		return plugin;
	}

	/**
	 *
	 * @param key - key is usually plug-in relative path to image like icons/xxx.gif
	 * @return Image loaded from key location or from registry cache, it will be stored in plug-in registry and disposed when plug-in unloads
	 */
	public Image getImage(String key) {
		ImageRegistry registry = getImageRegistry();
		Image image = registry.get(key);
		if (image == null) {
			ImageDescriptor descriptor = ResourceLocator.imageDescriptorFromBundle(PLUGIN_ID, key).get();
			if (descriptor == null) {
				ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
				return sharedImages.getImage(key);
			}
			registry.put(key, descriptor);
			image = registry.get(key);
		}
		return image;
	}

	/**
	 * Logs the specified status with this plug-in's log.
	 *
	 * @param status
	 *        status to log
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	/**
	 * Logs an internal error with the specified {@code Throwable}.
	 *
	 * @param e
	 *        the exception to be logged
	 */
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, 1, "Internal Error", e)); //$NON-NLS-1$
	}

	/**
	 * Logs an internal error with the specified message.
	 *
	 * @param message
	 *        the error message to log
	 */
	public static void log(String message) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, 1, message, null));
	}

	/**
	 * Logs an internal error with the specified message and {@code Throwable}.
	 *
	 * @param message
	 *        the error message to log
	 * @param e
	 *        the exception to be logged
	 */
	public static void log(String message, Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, 1, message, e));
	}

	/**
	 * @return
	 */
	public IPreferenceStore getCorePreferenceStore() {
		if (corePreferenceStore == null) {
			corePreferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, CodanCorePlugin.PLUGIN_ID);
		}
		return corePreferenceStore;
	}

	public IPreferenceStore getPreferenceStore(IProject project) {
		ProjectScope ps = new ProjectScope(project);
		ScopedPreferenceStore scoped = new ScopedPreferenceStore(ps, CodanCorePlugin.PLUGIN_ID);
		scoped.setSearchContexts(new IScopeContext[] { ps, InstanceScope.INSTANCE });
		return scoped;
	}
}
