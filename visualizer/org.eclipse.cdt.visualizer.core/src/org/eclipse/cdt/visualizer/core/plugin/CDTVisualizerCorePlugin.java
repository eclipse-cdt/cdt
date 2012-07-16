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

package org.eclipse.cdt.visualizer.core.plugin;

import org.eclipse.cdt.visualizer.core.ResourceManager;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


// ----------------------------------------------------------------------------
// CDTVisualizerCorePlugin
// ----------------------------------------------------------------------------

/**
 * CDT visualizer core plugin class.
 * 
 * This plugin contains the non-UI components of the visualizer framework.
 */
public class CDTVisualizerCorePlugin extends AbstractUIPlugin
{
	// --- constants ---
	
	/** The plug-in ID */
	public static final String PLUGIN_ID = "org.eclipse.cdt.visualizer.core"; //$NON-NLS-1$


	// --- static members ---

	/** Singleton instance */
	protected static CDTVisualizerCorePlugin s_plugin;

	/** Returns the singleton instance */
	public static CDTVisualizerCorePlugin getDefault() {
		return s_plugin;
	}

	/** Resource manager */
	protected static ResourceManager s_resources = null;


	// --- constructors/destructors ---
	
	/** Constructor */
	public CDTVisualizerCorePlugin() {
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
	public ResourceManager getPluginResources() {
		if (s_resources == null) {
			s_resources = new ResourceManager(this);
		}
		return s_resources;
	}
	
	/** Releases resource manager for this plugin. */
	public void cleanupPluginResources() {
		s_resources.dispose();
	}
	
	/** Convenience method for getting plugin resource manager */
	public static ResourceManager getResources() {
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
}
