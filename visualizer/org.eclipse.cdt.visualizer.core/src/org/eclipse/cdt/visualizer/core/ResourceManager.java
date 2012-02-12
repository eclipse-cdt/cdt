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

package org.eclipse.cdt.visualizer.core;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.Plugin;


// ---------------------------------------------------------------------------
// ResourceManager
// ---------------------------------------------------------------------------

/**
 * Plugin resource manager.
 * This class should be instanced in the plugin's "start()" method,
 * and disposed in the "stop()" method.
 */
public class ResourceManager{
	
	// --- members ---
	
	/** Plugin ID */
	protected String m_pluginID = null;
	
	/** Plugin */
	protected Plugin m_plugin = null;
	
	/** Parent resource manager, if any */
	protected ResourceManager m_parentManager = null;
	
	/** String resource manager */
	protected ResourceBundle m_stringResources = null;
	
	/** String resource property file name */
	protected String m_stringResourceFilename = null;
	
	
	// --- constructors/destructors ---
	
	/**
	 * Constructor,
	 * Assumes string resources are in the file "messages.properties".
	 */
	public ResourceManager(Plugin plugin) {
		this(plugin, "messages.properties");
	}

	/** Constructor */
	public ResourceManager(Plugin plugin, String stringResourceFilename) {
		m_pluginID = plugin.getBundle().getSymbolicName();
		m_plugin = plugin;
		m_stringResourceFilename = stringResourceFilename;
		getStringRegistry(); // creates registry object
	}
	
	/** Dispose method */
	public void dispose() {
		disposeStringRegistry();
		m_stringResourceFilename = null;
		m_plugin = null;
		m_pluginID = null;
	}
	
	
	// --- accessors ---
	
	/** Returns plugin we're associated with */
	public Plugin getPlugin() {
		return m_plugin;
	}
	
	
	// --- parent manager management ---
	
	/** Sets parent resource manager, if any */
	public void setParentManager(ResourceManager parentManager) {
		m_parentManager = parentManager;
	}

	/** Gets parent resource manager, if any */
	public ResourceManager getParentManager() {
		return m_parentManager;
	}

	
	// --- string resource management ---

	/** Creates/returns string registry */
	protected ResourceBundle getStringRegistry() {
		if (m_stringResources == null) {
			String filename = m_stringResourceFilename;

			// The ".properties" extension is assumed, so we trim it here
			String propertiesExtension = ".properties";
			if (filename.endsWith(propertiesExtension)) {
				filename = filename.substring(0, filename.length() -
												 propertiesExtension.length());
			}
			
			// NOTE: We have to be careful to pick up the class loader
			// from the plugin class, otherwise we default to the classloader
			// of the ResourceManager class, which is the classloader
			// for the plugin the ResourceManager comes from.
			ClassLoader classLoader = m_plugin.getClass().getClassLoader();
			Locale locale = Locale.getDefault();
			
			// we'll check for .properties file first
			// in the same directory as the plugin activator class
			String propertyFileName1 = m_pluginID + ".plugin." + filename;
			try {
				m_stringResources =
					ResourceBundle.getBundle(propertyFileName1,
						locale, classLoader);
			}
			catch (MissingResourceException e) {
				// TODO: log this exception (probably a .properties file is missing)
				m_stringResources = null;
			}
			
			// if not found, we try in the default package 
			// (that is, the top-level "src" or "resources" folder)
			String propertyFileName2 = filename;
			if (m_stringResources == null) {
				try {
					m_stringResources =
						ResourceBundle.getBundle(propertyFileName2,
								locale, classLoader);
				}
				catch (MissingResourceException e) {
					// TODO: log this exception (probably a .properties file is missing)
					m_stringResources = null;
				}
			}
		}
		return m_stringResources;
	}
	
	/** Disposes of string registry */
	protected void disposeStringRegistry() {
		m_stringResources = null;
	}
	
	/** Returns string resource for specified key */
	public String getString(String key) {
		String result = null;
		if (key == null) return "(null resource)";
		
		// get string registry, look up key
		ResourceBundle strings = getStringRegistry();
		if (strings == null) {
			// if we can't get the registry, display the key instead,
			// so we know what's missing (e.g. the .properties file)
			result = "(" + key + ")";
		}
		else {
			try {
				result = strings.getString(key);
			}
			catch (MissingResourceException e) {
				// we fail, but don't throw an exception
				// so we don't screw any UI setup that depends
				// on this string resource
				result = null;
			}
		}

		// if we fail, try the parent manager if there is one
		if (result == null && m_parentManager != null) {
			result = m_parentManager.getString(key);
		}
		
		// if we still fail, display the key instead,
		// so we know what's missing
		if (result == null) result = "[" + key + "]";
		return result;
	}
	
	/** Formats string resource with specified argument(s) */
	public String getString(String key, Object... arguments) {
		return MessageFormat.format(getString(key), arguments);
	}
}
