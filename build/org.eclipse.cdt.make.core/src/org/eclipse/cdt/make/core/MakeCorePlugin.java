package org.eclipse.cdt.make.core;
/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 * IBM Rational Software - Initial API and implementation
***********************************************************************/

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;

/**
 * The main plugin class to be used in the desktop.
 */
public class MakeCorePlugin extends Plugin {
	public static final String OLD_BUILDER_ID = "org.eclipse.cdt.core.cbuilder";
	//The shared instance.
	private static MakeCorePlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;

	/**
	 * The constructor.
	 */
	public MakeCorePlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("org.eclipse.cdt.make.core.PluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	/**
	 * Returns the shared instance.
	 */
	public static MakeCorePlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = MakeCorePlugin.getDefault().getResourceBundle();
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}

	public static String getFormattedString(String key, String arg) {
		return MessageFormat.format(getResourceString(key), new String[] { arg });
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return "org.eclipse.cdt.make.core"; //$NON-NLS-1$
		}
		return getDefault().getDescriptor().getUniqueIdentifier();
	}

	protected void initializeDefaultPluginPreferences() {
		IMakeBuilderInfo info = BuildInfoFactory.create(getPluginPreferences(), MakeBuilder.BUILDER_ID, true);
		try {
			info.setBuildCommand(new Path("make"));
			info.setBuildLocation(new Path(""));
			info.setStopOnError(false);
			info.setUseDefaultBuildCmd(true);
			info.setAutoBuildEnable(false);
			info.setAutoBuildTarget("all");
			info.setIncrementalBuildEnable(true);
			info.setIncrementalBuildTarget("all");
			info.setFullBuildEnable(true);
			info.setFullBuildTarget("clean all");
		} catch (CoreException e) {
		}
		getPluginPreferences().setDefault(CCorePlugin.PREF_BINARY_PARSER, CCorePlugin.PLUGIN_ID + ".ELF");
	}
}
