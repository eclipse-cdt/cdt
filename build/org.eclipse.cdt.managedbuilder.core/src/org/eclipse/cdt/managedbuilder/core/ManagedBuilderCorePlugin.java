package org.eclipse.cdt.managedbuilder.core;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 * **********************************************************************/

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.cdt.managedbuilder.scannerconfig.IManagedScannerInfoCollector;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Plugin;


public class ManagedBuilderCorePlugin extends Plugin {
	//The shared instance.
	private static ManagedBuilderCorePlugin plugin;
	//Resource bundle.
	private static ResourceBundle resourceBundle;

	/**
	 * @param descriptor
	 */
	public ManagedBuilderCorePlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("org.eclipse.cdt.managedbuilder.internal.core.PluginResources"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	/**
	 * Returns the shared instance.
	 */
	public static ManagedBuilderCorePlugin getDefault() {
		return plugin;
	}


	public static String getResourceString(String key) {
		try {
			return resourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
		} catch (NullPointerException e) {
			return "#" + key + "#"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public static String getFormattedString(String key, String arg) {
		return MessageFormat.format(getResourceString(key), new String[] { arg });
	}

	public static String getFormattedString(String key, String[] args) {
		return MessageFormat.format(getResourceString(key), args);
	}

	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 */
	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return "org.eclipse.cdt.managedbuilder.core"; //$NON-NLS-1$
		}
		return getDefault().getDescriptor().getUniqueIdentifier();
	}

	/**
	 * Targets may have a scanner collector defined that knows how to discover 
	 * built-in compiler defines and includes search paths. Find the scanner 
	 * collector implentation for the target specified.
	 * 
	 * @param string the unique id of the target to search for
	 * @return an implementation of <code>IManagedScannerInfoCollector</code>
	 */
	public IManagedScannerInfoCollector getScannerInfoCollector(String targetId) {
		try {
			IExtensionPoint extension = getDescriptor().getExtensionPoint(ManagedBuildManager.EXTENSION_POINT_ID);
			if (extension != null) {
				// There could be many of these
				IExtension[] extensions = extension.getExtensions();
				for (int i = 0; i < extensions.length; i++) {
					IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
					for (int j = 0; j < configElements.length; j++) {
						IConfigurationElement element = configElements[j];
						if (element.getName().equals("target")) { //$NON-NLS-1$
							if (element.getAttribute(ITarget.ID).equals(targetId)) {
								return (IManagedScannerInfoCollector) element.createExecutableExtension("scannerInfoCollector"); //$NON-NLS-1$
							}
						}
					}
				}
			}
		} 
		catch (CoreException e) {
			// Probably not defined
		}
		return null;
	}

}
