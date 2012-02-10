/*******************************************************************************
 * Copyright (c) 2012 Tilera Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation)
 *******************************************************************************/

package org.eclipse.cdt.visualizer.ui.plugin;

import org.eclipse.cdt.visualizer.core.plugin.CDTVisualizerCorePlugin;
import org.eclipse.cdt.visualizer.ui.util.Colors;
import org.eclipse.cdt.visualizer.ui.util.UIResourceManager;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


// ----------------------------------------------------------------------------
// CDTVisualizerUIPlugin
// ----------------------------------------------------------------------------

/**
 * CDT visualizer UI plugin class.
 * 
 * This plugin contains the UI components of the visualizer framework.
 */
public class CDTVisualizerUIPlugin extends AbstractUIPlugin
{
	// --- constants ---
	
    /** Feature ID (used as prefix for extension points, etc). */
    public static final String FEATURE_ID = "org.eclipse.cdt.visualizer.ui"; //$NON-NLS-1$
	
	/** The plug-in ID */
	public static final String PLUGIN_ID = "org.eclipse.cdt.visualizer.ui"; //$NON-NLS-1$

	
	// --- static members ---

	/** Singleton instance */
	protected static CDTVisualizerUIPlugin s_plugin;

	/** Returns the singleton instance */
	public static CDTVisualizerUIPlugin getDefault() {
		return s_plugin;
	}

	/** Resource manager */
	protected static UIResourceManager s_resources = null;

	
	// --- constructors/destructors ---
	
	/**
	 * Constructor
	 */
	public CDTVisualizerUIPlugin() {
	}

	
	// --- plugin startup/shutdown methods ---

	/** Invoked when plugin is loaded. */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		s_plugin = this;
		
		// touch activator classes of any plugins we depend on,
		// to ensure their start() methods are called first
		// (None for now.)
		
		// initialize resource management (strings, images, fonts, colors, etc.)
		getPluginResources();
	}

	/** Invoked when plugin is stopped. */
	public void stop(BundleContext context) throws Exception {
		// clean up resource management
		cleanupPluginResources();
		
		s_plugin = null;
		super.stop(context);
	}
	
	
	// --- logging ---
	
	/** 
	 * Writes message to Eclipse log.
	 * Severity can be one of:
	 * Status.OK, Status.ERROR, Status.INFO, Status.WARNING, Status.CANCEL
	 */
	public static void log(int severity, String text)
	{
		Status status = new Status(severity, PLUGIN_ID, text);
		ResourcesPlugin.getPlugin().getLog().log(status);
	}

	
	// --- resource management ---
	
	/** Returns resource manager for this plugin */
	public UIResourceManager getPluginResources() {
		if (s_resources == null) {
			s_resources = new UIResourceManager(this);
			s_resources.setParentManager(CDTVisualizerCorePlugin.getResources());

			// initialize Colors class, now that UIResources object is available.
			Colors.initialize(s_resources);
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
