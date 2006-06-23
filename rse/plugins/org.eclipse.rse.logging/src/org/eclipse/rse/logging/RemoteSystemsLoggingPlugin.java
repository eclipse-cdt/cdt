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
import org.eclipse.core.runtime.Preferences;
import org.osgi.framework.BundleContext;


/**
 * Remote Systems Logging plugin.
 */
public class RemoteSystemsLoggingPlugin extends Plugin {


	//The shared instance.
	private static RemoteSystemsLoggingPlugin inst;

	//Resource bundle.
	private ResourceBundle resourceBundle;

	// The cached Logger inst.
	public static Logger out = null;

	/**
	 * Constructor.
	 */
	public RemoteSystemsLoggingPlugin() {
		super();
		
		if (inst == null) {
		    inst = this;
		}
	}

	/**
	 * Returns the shared plugin instance.
	 */
	public static RemoteSystemsLoggingPlugin getDefault() {
		return inst;
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

	/** 
	 * Sets default preference values.
	 */
	public void initializeDefaultPreferences() {
		Preferences prefs = getPluginPreferences();
		prefs.setDefault(IRemoteSystemsLogging.DEBUG_LEVEL, IRemoteSystemsLogging.LOG_ERROR);
		prefs.setDefault(IRemoteSystemsLogging.LOG_LOCATION, IRemoteSystemsLogging.LOG_TO_FILE);
	}

    /**
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        
		// don't need a preference page for this plugin.
		out = LoggerFactory.getInst(this);
		out.logInfo("loading RemoteSystemsLoggingPlugin class.");
    }
    
    /**
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
		LoggerFactory.freeInst(this);
        super.stop(context);
    }
}