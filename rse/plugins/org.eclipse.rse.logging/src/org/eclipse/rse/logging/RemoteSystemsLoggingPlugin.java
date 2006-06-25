/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.logging;

import java.net.URL;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * Remote Systems Logging plugin.
 */
public class RemoteSystemsLoggingPlugin extends Plugin {

	private static RemoteSystemsLoggingPlugin singleton;
	private ResourceBundle resourceBundle;
	public static Logger out = null;

	/**
	 * Constructor.
	 */
	public RemoteSystemsLoggingPlugin() {
		super();
		singleton = this;
	}

	/**
	 * Returns the shared plugin instance.
	 */
	public static RemoteSystemsLoggingPlugin getDefault() {
		return singleton;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		try {
			ResourceBundle bundle = RemoteSystemsLoggingPlugin.getDefault().getResourceBundle();
			return bundle.getString(key);
		} catch (Exception e) {
			out.logError("could not get resource string for: " + key, e);
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle.
	 */
	public ResourceBundle getResourceBundle() {
		if (resourceBundle == null) {
			try {
				IPath path = new Path("$nl$/RemoteSystemsLogging.properties");
				URL url = FileLocator.find(getBundle(), path, null);
				resourceBundle = new PropertyResourceBundle(url.openStream());
			} catch (Exception x) {
				resourceBundle = null;
				out.logInfo("RemoteSystemsLoggingPlugin - unable to log resourcebundle");
			}
		}
		return resourceBundle;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		out = LoggerFactory.getLogger(this);
		out.logInfo("loading RemoteSystemsLoggingPlugin class.");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		LoggerFactory.freeLogger(this);
		super.stop(context);
	}
}