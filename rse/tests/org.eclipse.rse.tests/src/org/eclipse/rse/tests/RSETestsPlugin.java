/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Don Yantzi (IBM) - initial contribution.
 * David Dykstal (IBM) - initial contribution.
 * Uwe Stieber (Wind River) - restructuring and cleanup
 * Martin Oberhuber (Wind River) - [168870] refactor org.eclipse.rse.core package of the UI plugin
 ********************************************************************************/
package org.eclipse.rse.tests;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.tests.core.IRSETestLogCollectorDelegate;
import org.eclipse.rse.tests.internal.RSEDefaultTestLogCollectorDelegate;
import org.eclipse.rse.tests.internal.testsubsystem.TestSubSystemAdapterFactory;
import org.eclipse.rse.tests.testsubsystem.interfaces.ITestSubSystem;
import org.eclipse.rse.tests.testsubsystem.interfaces.ITestSubSystemConfiguration;
import org.eclipse.rse.tests.testsubsystem.interfaces.ITestSubSystemNode;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.osgi.framework.BundleContext;

/**
 * Main plugin class for the RSE JUnit tests framework. This
 * class provides basic infra structure for accessing externalized
 * string data.
 */
public class RSETestsPlugin extends SystemBasePlugin {
	// The shared plugin instance.
	private static RSETestsPlugin plugin;
	// The resource bundle associated with this plugin.
	private ResourceBundle resourceBundle;

	// Test log collector delegates storage.
	private final List logCollectorDelegates = new ArrayList();
	
	// Default test log collector delegate
	private final IRSETestLogCollectorDelegate defaultLogCollectorDelegate = new RSEDefaultTestLogCollectorDelegate();
	
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
			return MessageFormat.format(resourceString, arguments);
		}
		return resourceString;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		addDelegate(defaultLogCollectorDelegate);

		IAdapterManager manager = Platform.getAdapterManager();
		TestSubSystemAdapterFactory subSystemAdapterFactory = new TestSubSystemAdapterFactory();
		manager.registerAdapters(subSystemAdapterFactory, ITestSubSystem.class);
		manager.registerAdapters(subSystemAdapterFactory, ITestSubSystemNode.class);
		manager.registerAdapters(subSystemAdapterFactory, ITestSubSystemConfiguration.class);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		removeDelegate(defaultLogCollectorDelegate);
		super.stop(context);
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
		if (value != null) return Boolean.getBoolean(value);
			
		// If the system property is not set, check for the key in the resource bundle
		value = getResourceString(testId);
		if (value != null && !value.startsWith("!")) return Boolean.valueOf(value).booleanValue(); //$NON-NLS-1$

		// the test is considered enabled as well if not otherwise explicitly overriden
		return true;
	}
	
	/**
	 * Add the specified test collector delegate to the list. If the specified
	 * delegate had been already added to the list before, the method will return
	 * without re-adding the test collector delegate again.
	 * 
	 * @param delegate The test collector delegate to add. Must be not <code>null</code>.
	 */
	public synchronized void addDelegate(IRSETestLogCollectorDelegate delegate) {
		assert delegate != null;
		if (delegate != null && !logCollectorDelegates.contains(delegate)) {
			logCollectorDelegates.add(delegate);
		}
	}
	
	/**
	 * Removes the specified test collector delegate from the list. If the specified
	 * delegate had not been added to the list before, the method will return immediatelly.
	 * 
	 * @param delegate The test collector delegate to remove. Must be not <code>null</code>.
	 */
	public synchronized void removeDelegate(IRSETestLogCollectorDelegate delegate) {
		assert delegate != null;
		if (delegate != null) {
			logCollectorDelegates.remove(delegate);
		}
	}

	/**
	 * Returns the currently list of known test log collector delegates.
	 * 
	 * @return The currently known list of test collector delegates.
	 */
	public synchronized IRSETestLogCollectorDelegate[] getTestLogCollectorDelegates() {
		return (IRSETestLogCollectorDelegate[])logCollectorDelegates.toArray(new IRSETestLogCollectorDelegate[logCollectorDelegates.size()]);
	}

	/**
	 * Initialize the image registry by declaring all of the required graphics.
	 */
	protected void initializeImageRegistry() {
		String path = getIconPath();
		putImageInRegistry("ICON_ID_BRANCH", path + "branch.gif"); //$NON-NLS-1$ //$NON-NLS-2$
		putImageInRegistry("ICON_ID_LEAF", path + "leaf.gif"); //$NON-NLS-1$ //$NON-NLS-2$
	}
}

