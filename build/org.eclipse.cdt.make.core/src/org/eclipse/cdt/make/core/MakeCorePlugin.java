/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.core;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.make.internal.core.BuildInfoFactory;
import org.eclipse.cdt.make.internal.core.MakeTargetManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;

/**
 * The main plugin class to be used in the desktop.
 */
public class MakeCorePlugin extends Plugin {
	private MakeTargetManager fTargetManager;
	public static final String OLD_BUILDER_ID = "org.eclipse.cdt.core.cbuilder"; //$NON-NLS-1$
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
			resourceBundle = ResourceBundle.getBundle("org.eclipse.cdt.make.core.PluginResources"); //$NON-NLS-1$
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

	public static void log(Throwable e) {
		if (e instanceof InvocationTargetException)
			e = ((InvocationTargetException) e).getTargetException();
		IStatus status = null;
		if (e instanceof CoreException)
			status = ((CoreException) e).getStatus();
		else
			status = new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.OK, e.getMessage(), e);
		log(status);
	}

	public static void log(IStatus status) {
		ResourcesPlugin.getPlugin().getLog().log(status);
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
		IMakeBuilderInfo info = createBuildInfo(getPluginPreferences(), MakeBuilder.BUILDER_ID, true);
		try {
			info.setBuildCommand(new Path("make")); //$NON-NLS-1$
			info.setBuildLocation(new Path("")); //$NON-NLS-1$
			info.setStopOnError(false);
			info.setUseDefaultBuildCmd(true);
			info.setAutoBuildEnable(false);
			info.setAutoBuildTarget("all"); //$NON-NLS-1$
			info.setIncrementalBuildEnable(true);
			info.setIncrementalBuildTarget("all"); //$NON-NLS-1$
			info.setFullBuildEnable(true);
			info.setFullBuildTarget("clean all"); //$NON-NLS-1$
		} catch (CoreException e) {
		}
		getPluginPreferences().setDefault(CCorePlugin.PREF_BINARY_PARSER, CCorePlugin.PLUGIN_ID + ".ELF"); //$NON-NLS-1$
	}
	
	public static IMakeBuilderInfo createBuildInfo(Preferences prefs, String builderID, boolean useDefaults) {
		return BuildInfoFactory.create(prefs, builderID, useDefaults);
	}

	public static IMakeBuilderInfo createBuildInfo(IProject project, String builderID) throws CoreException {
		return BuildInfoFactory.create(project, builderID);
	}

	public static IMakeBuilderInfo createBuildInfo(Map args, String builderID) {
		return BuildInfoFactory.create(args, builderID);
	}

	public IMakeTargetManager getTargetManager() {
		if ( fTargetManager == null) {
			fTargetManager = new MakeTargetManager();
			fTargetManager.startup();
		}
		return fTargetManager;
	}
	public void shutdown() throws CoreException {
		super.shutdown();
		if ( fTargetManager != null) {
			fTargetManager.shutdown();
			fTargetManager = null;
		}
	}
}
