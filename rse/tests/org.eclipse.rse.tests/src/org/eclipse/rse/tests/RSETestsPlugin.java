/* *******************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Don Yantzi (IBM) - initial contribution.
 * David Dykstal (IBM) - initial contribution.
 * Uwe Stieber (Wind River) - restructuring and cleanup
 * *******************************************************************************/
package org.eclipse.rse.tests;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Main plugin class for the RSE JUnit tests framework. This
 * class provides basic infra structure for accessing externalized
 * string data.
 */
public class RSETestsPlugin extends AbstractUIPlugin {
	// The shared plugin instance.
	private static RSETestsPlugin plugin;
	// The resource bundle associated with this plugin.
	private ResourceBundle resourceBundle;

	/**
	 * Constructor.
	 */
	public RSETestsPlugin() {
		super();
		plugin = this;
	}
	
	/**
	 * Returns the shared plugin instance.
	 * 
	 * @return The plugin instance or <code>null</code> if not yet constructed.
	 */
	public static RSETestsPlugin getDefault() {
		return plugin;
	}
	
	/**
	 * Returns the associated resource bundle. If necessary, the resource
	 * bundle will be initialized.
	 * 
	 * @return The resource bundle instance.
	 */
	public ResourceBundle getResourceBundle() {
		// If the resource bundle got created already, return the
		// existing instance.
		if (resourceBundle != null) return resourceBundle;
		
		// The resource bundle had not been created yet -> create it.
		resourceBundle = ResourceBundle.getBundle("org.eclipse.rse.tests.RSETestsResources"); //$NON-NLS-1$
		return resourceBundle;
	}
	
	/**
	 * Queries the externalized string for the specified resource key from
	 * the plugins associated resource bundle.
	 * 
	 * @param key The resource key. Must be not <code>null</code>!
	 * @return The externalized string or the resource key enclosed in exlamation marks.
	 */
	public static String getResourceString(String key) {
		assert key != null;
		
		// Query the resource bundle from the plugin instance.
		ResourceBundle bundle = RSETestsPlugin.getDefault().getResourceBundle();
		if (bundle != null) {
			try {
				// Lookup the key and return the corresponding string if found.
				return bundle.getString(key);
			} catch (MissingResourceException e) {
				// return the key as is enclosed in exlamation marks.
			}
		}
		
		// If we could not found the key or the bundle is invalid,
		// return the key as is enclosed in exlamation marks.
		return '!' + key + '!';
	}
	
	/**
	 * Queries the externalized string for the specified resource key from
	 * the plugins associated resource bundle. If a externalized resource
	 * for the specified key exist, a possible argument placeholder will be
	 * replaced by the specified value.
	 * 
	 * @param key The resource key. Must be not <code>null</code>!
	 * @param argument The content for a possible placeholder. Must be not <code>null</code>.
	 * @return The externalized string or the resource key enclosed in exlamation marks.
	 */
	public static String getResourceString(String key, Object argument) {
		assert argument != null;
		return getResourceString(key, new Object[] { argument });
	}
	
	/**
	 * Queries the externalized string for the specified resource key from
	 * the plugins associated resource bundle. If a externalized resource
	 * for the specified key exist, possible argument placeholder will be
	 * replaced by their specified values.
	 * 
	 * @param key The resource key. Must be not <code>null</code>!
	 * @param arguments The content for the possible arguments. Must be not <code>null</code>.
	 * @return The externalized string or the resource key enclosed in exlamation marks.
	 */
	public static String getResourceString(String key, Object[] arguments) {
		assert arguments != null;
		String resourceString = getResourceString(key);
		if (!resourceString.startsWith("!")) { //$NON-NLS-1$
			MessageFormat.format(resourceString, arguments);
		}
		return resourceString;
	}
	
	/**
	 * Checks if the test case given through the specified key is enabled for
	 * execution. A test case is considered enabled if either<br>
	 * <ul>
	 * 	<li>-D&lt;testId&gt; is true or</li>
	 *  <li>getResourceString(&lt;testId&gt;) is true or</li>
	 *  <li>neither the explicit -D option is specified nor the key exist in the associated
	 *      resource bundle.</li>
	 * </ul>
	 * 
	 * @param testId The unique string id of the test case to execute. Must be not <code>null</code>!
	 * @return <code>true</code> if the test case is enabled for execution, <code>false</code> otherwise.
	 */
	public static boolean isTestCaseEnabled(String testId) {
		assert testId != null;
		// Test first for the system property (explicit -D option).
		String value = System.getProperty(testId);
//		if (value != null) return Boolean.parseBoolean(value);
		if (value != null) return value.equals("true"); //$NON-NLS-1$
		
		// If the system property is not set, check for the key in the resource bundle
		value = getResourceString(testId);
//		if (value != null && !value.startsWith("!")) return Boolean.parseBoolean(value); //$NON-NLS-1$
		if (value != null && !value.startsWith("!")) return value.equals("true"); //$NON-NLS-1$ //$NON-NLS-2$
		return false;
	}
}
