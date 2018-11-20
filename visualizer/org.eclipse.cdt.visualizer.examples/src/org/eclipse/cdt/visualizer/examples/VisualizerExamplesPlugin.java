/*******************************************************************************
 * Copyright (c) 2012 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Khouzam (Ericsson)     - initial API and implementation
 *     William R. Swanson (Tilera) - added resource manager
 *******************************************************************************/
package org.eclipse.cdt.visualizer.examples;

import org.eclipse.cdt.visualizer.ui.plugin.CDTVisualizerUIPlugin;
import org.eclipse.cdt.visualizer.ui.util.UIResourceManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class VisualizerExamplesPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.visualizer.examples"; //$NON-NLS-1$

	// The shared instance
	private static VisualizerExamplesPlugin plugin;

	/** Resource manager */
	protected static UIResourceManager s_resources = null;

	/**
	 * The constructor
	 */
	public VisualizerExamplesPlugin() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		// initialize resource management (strings, images, fonts, colors, etc.)
		getPluginResources();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// clean up resource management
		cleanupPluginResources();

		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static VisualizerExamplesPlugin getDefault() {
		return plugin;
	}

	// --- resource management ---

	/** Returns resource manager for this plugin */
	public UIResourceManager getPluginResources() {
		if (s_resources == null) {
			s_resources = new UIResourceManager(this);
			s_resources.setParentManager(CDTVisualizerUIPlugin.getResources());
		}

		return s_resources;
	}

	/** Releases resource manager for this plugin. */
	public void cleanupPluginResources() {
		s_resources.dispose();
	}

	/** Convenience method for getting plugin resource manager */
	public static UIResourceManager getResources() {
		return getDefault().getPluginResources();
	}

	/** Convenience method for looking up string resources */
	public static String getString(String key) {
		return getDefault().getPluginResources().getString(key);
	}

	/** Convenience method for looking up string resources */
	public static String getString(String key, Object... arguments) {
		return getDefault().getPluginResources().getString(key, arguments);
	}

	/** Convenience method for looking up image resources */
	public static Image getImage(String key) {
		return getDefault().getPluginResources().getImage(key);
	}

	/** Convenience method for looking up image resources */
	public static ImageDescriptor getImageDescriptor(String key) {
		return getDefault().getPluginResources().getImageDescriptor(key);
	}

	/** Convenience method for looking up font resources */
	public static Font getFont(String fontName, int height) {
		return getDefault().getPluginResources().getFont(fontName, height);
	}

	/** Convenience method for looking up font resources */
	public static Font getFont(String fontName, int height, int style) {
		return getDefault().getPluginResources().getFont(fontName, height, style);
	}
}
