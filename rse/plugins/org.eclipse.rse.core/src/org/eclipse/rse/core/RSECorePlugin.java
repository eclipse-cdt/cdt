/********************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others. All rights reserved.
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
 * David Dykstal (IBM) - added utility method for finding qualifiedHostNames
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * Martin Oberhuber (Wind River) - [186525] Move keystoreProviders to core
 * Martin Oberhuber (Wind River) - [181939] Deferred class loading for keystoreProviders
 * Martin Oberhuber (Wind River) - [175680] Deprecate obsolete ISystemRegistry methods
 * Martin Oberhuber (Wind River) - [160293] NPE on startup when only Core feature is installed
 * Uwe Stieber (Wind River) - [192611] RSE Core plugin may fail to initialize because of cyclic code invocation
 * Martin Oberhuber (Wind River) - [165674] Sort subsystem configurations by priority then Id
 * Martin Oberhuber (Wind River) - [215820] Move SystemRegistry implementation to Core
 * David Dykstal (IBM) - [197167] adding notification and waiting for RSE model
 ********************************************************************************/
package org.eclipse.rse.core;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.rse.core.comm.SystemKeystoreProviderManager;
import org.eclipse.rse.core.model.ISystemProfileManager;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystemConfigurationProxy;
import org.eclipse.rse.internal.core.InitRSEJob;
import org.eclipse.rse.internal.core.RSECoreRegistry;
import org.eclipse.rse.internal.core.model.SystemProfileManager;
import org.eclipse.rse.internal.core.model.SystemRegistry;
import org.eclipse.rse.internal.core.subsystems.SubSystemConfigurationProxy;
import org.eclipse.rse.internal.core.subsystems.SubSystemConfigurationProxyComparator;
import org.eclipse.rse.internal.persistence.RSEPersistenceManager;
import org.eclipse.rse.logging.Logger;
import org.eclipse.rse.logging.LoggerFactory;
import org.eclipse.rse.persistence.IRSEPersistenceManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * RSECorePlugin provides the activation for the RSE core and acts as the primary
 * registry for logging, persistence, and the main RSE service registries.
 * It should not be extended by other classes.
 */
public class RSECorePlugin extends Plugin {

	/**
	 * The plugin id for this plugin. Value "org.eclipse.rse.core".
	 */
	public static final String PLUGIN_ID = "org.eclipse.rse.core"; //$NON-NLS-1$
	
	/**
	 * Current release as a number (multiplied by 100). E.g. 300 is for release 3.0.0
	 */
	public static final int CURRENT_RELEASE = 200; // updated to new release

	/**
	 * Current release as a string.
	 */
	public static final String CURRENT_RELEASE_NAME = "2.0.0";  //$NON-NLS-1$

	/**
	 * Value 1.
	 * Used in isInitComplete(int) which will return true if the model phase of the 
	 * initialization is complete.
	 * Clients must not assume any particular ordering 
	 * among phases based on the value.
	 */
	public static final int INIT_MODEL = 1;
	
	/**
	 * Value 999.
	 * Used in isInitComplete(int) which will return true if all phases of  
	 * initialization are complete.
	 * Clients must not assume any particular ordering 
	 * among phases based on the value.
	 */
	public static final int INIT_ALL = 999;
	
	private static RSECorePlugin plugin = null; // the singleton instance of this plugin
	private Logger logger = null;
	private ISystemRegistry _systemRegistry = null;
	private IRSEPersistenceManager _persistenceManager = null;
	private ISubSystemConfigurationProxy[] _subsystemConfigurations = null;
 
	/**
	 * Returns the singleton instance of RSECorePlugin.
	 * @return the singleton instance.
	 */
	public static RSECorePlugin getDefault() {
		return plugin;
	}
	
	/**
	 * Waits until the RSE model has been fully restored from its persistent form. Should be used 
	 * before accessing pieces of the model.
	 * @return an IStatus indicating how the initialization ended.
	 * @throws InterruptedException if this wait was interrupted for some reason.
	 */
	public static IStatus waitForInitCompletion() throws InterruptedException {
		return InitRSEJob.getInstance().waitForCompletion();
	}
	
	/**
	 * @param phase the phase identifier.
	 * @return true if initialization of the RSE model is complete for that phase.
	 * It will return true if the 
	 * initialization for that phase has completed regardless its status of that completion.
	 * @throws IllegalArgumentException if the phase is undefined.
	 * @see #INIT_ALL
	 * @see #INIT_MODEL
	 */
	public static boolean isInitComplete(int phase) {
		return InitRSEJob.getInstance().isComplete(phase);
	}
	
	/**
	 * Adds a new listener to the set of listeners to be notified when initialization phases complete.
	 * If the listener is added after the phase has completed it will not be invoked.
	 * If the listener is already in the set it will not be added again.
	 * Listeners may be notified in any order.
	 * @param listener the listener to be added
	 */
	public static void addInitListener(IRSEInitListener listener) {
		InitRSEJob.getInstance().addInitListener(listener);
	}
	
	/**
	 * Removes a listener to the set of listeners to be notified when phases complete.
	 * If the listener is not in the set this does nothing.
	 * @param listener the listener to be removed
	 */
	public static void removeInitListener(IRSEInitListener listener) {
		InitRSEJob.getInstance().removeInitListener(listener);
	}

	/**
	 * A static convenience method - fully equivalent to 
	 * <code>RSECorePlugin.getDefault().getRegistry()</code>.
	 * @return the RSE Core Registry.
	 */
	public static IRSECoreRegistry getTheCoreRegistry() {
		return getDefault().getCoreRegistry();
	}
	
	/**
	 * A static convenience method - fully equivalent to 
	 * <code>RSECorePlugin.getDefault().getPersistenceManager()</code>.
	 * @return the persistence manager currently in use for RSE
	 */
	public static IRSEPersistenceManager getThePersistenceManager() {
		return getDefault().getPersistenceManager();
	}
	
	/**
	 * Return the master profile manager singleton.
	 * @return the RSE Profile Manager Singleton.
	 */
	public static ISystemProfileManager getTheSystemProfileManager() {
		return SystemProfileManager.getDefault();
	}

    /**
     * Check if the SystemRegistry has been instantiated already.
     * Use this when you don't want to start the system registry as a
     * side effect of retrieving it.
     * @return <code>true</code> if the System Registry has been instantiated already.
     */
    public static boolean isTheSystemRegistryActive()
    {
    	if (plugin == null) {
      	  return false;
    	}
    	return getDefault().isSystemRegistryActive();
    }

	/**
	 * A static convenience method - fully equivalent to 
	 * <code>RSECorePlugin.getDefault().getSystemRegistry()</code>.
	 * The SystemRegistry is used to gain access to the basic services
	 * and components used in RSE. 
	 * @return the RSE System Registry.
	 */
	public static ISystemRegistry getTheSystemRegistry() {
		return getDefault().getSystemRegistry();
	}
	
	/**
	 * @return the IP host name of this machine
	 */
	public static String getLocalMachineName() {
		String machineName = null;
		try {
			machineName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			getDefault().log(e);
		}
		return machineName;
	}

	/**
	 * @return the local IP address of this machine
	 */
	public static String getLocalMachineIPAddress() {
		String machineAddress = null;
		try {
			machineAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			getDefault().log(e);
		}
		return machineAddress;
	}

	/**
	 * Returns a qualified host name given a potentially unqualified host name
	 */
	public static String getQualifiedHostName(String hostName) {
		try {
			InetAddress address = InetAddress.getByName(hostName);
			return address.getCanonicalHostName();
		} catch (UnknownHostException exc) {
			return hostName;
		}
	}

	/**
	 * The constructor. This may be called only by plugin activation.
	 */
	public RSECorePlugin() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		registerKeystoreProviders();
		InitRSEJob job = InitRSEJob.getInstance();
		job.schedule();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		LoggerFactory.freeLogger(this);
		logger = null;
		plugin = null;
	}

	/**
	 * @return the persistence manager used for persisting RSE profiles
	 */
	public IRSEPersistenceManager getPersistenceManager() {
		if (_persistenceManager == null) {
			synchronized(this) {
				if (_persistenceManager==null) {
					_persistenceManager = new RSEPersistenceManager(getSystemRegistry());
				}
			}
		}
		return _persistenceManager;
	}

    /**
     * Test if the SystemRegistry has been instantiated already.
     * Use this when you don't want to start the system registry as a
     * side effect of retrieving it.
     * @return <code>true</code> if the system registry has been instantiated already.
     */
    private boolean isSystemRegistryActive()
    {
    	return (_systemRegistry != null);
    }
    
	/**
     * Return the SystemRegistry singleton.
     * Clients should use static @{link getTheSystemRegistry()} instead.
	 * @return the RSE system registry
	 */
	public ISystemRegistry getSystemRegistry() {
    	if (_systemRegistry == null) {
    		synchronized(this) {
    			if (_systemRegistry == null) {
    	    		String logfilePath = getStateLocation().toOSString();    	
    	    		SystemRegistry sr = SystemRegistry.getInstance(logfilePath);
    	    		ISubSystemConfigurationProxy[] proxies = getSubSystemConfigurationProxies();
    	    		if (proxies != null) {
    	    			sr.setSubSystemConfigurationProxies(proxies);
    	    		}
    	    		_systemRegistry = sr;
    			}
    		}
        }
		return _systemRegistry;
	}

	/**
	 * Returns the RSE core registry. Clients should use this method to get the registry which
	 * is the starting point for working with the RSE framework. It contains methods to access
	 * core services and components. It is distinct from, and more basic than, an implementation
	 * of ISystemRegistry.
	 * <p>
	 * This may return null if the registry has not yet been set.
	 * @return the RSE core registry.
	 */
	public IRSECoreRegistry getCoreRegistry() {
		return RSECoreRegistry.getInstance();
	}
	
	/**
	 * Returns an instance of the logger being used by the core. All core services, or extensions to 
	 * core services, should use this logger to log any messages. The RSE logger provides run-time 
	 * filtering according to user preference and uses the platform's logging capabilities. RSE services
	 * should use this logger rather than a platform logger. The logger is defined at plugin start and 
	 * should always be available.
	 * @return the instance of System#Logger used by the core RSE
	 */
	public Logger getLogger() {
		if (logger == null) logger = LoggerFactory.getLogger(this);
		return logger;
	}
	
	/**
	 * Log an unexpected exception that occurs during the functioning of this class.
	 * @param t the exception to log
	 */
	private void log(Throwable t) {
		getLogger().logError("Unexpected Exception", t); //$NON-NLS-1$
	}

	/**
	 * Register declared keystore providers.
	 */
	private void registerKeystoreProviders()
	{
		// Get reference to the plug-in registry
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		
		// Get configured extenders
		IConfigurationElement[] systemTypeExtensions = registry.getConfigurationElementsFor("org.eclipse.rse.core", "keystoreProviders"); //$NON-NLS-1$  //$NON-NLS-2$
		     	
		for (int i = 0; i < systemTypeExtensions.length; i++) 
		{
			// get the name space of the declaring extension
		    String nameSpace = systemTypeExtensions[i].getDeclaringExtension().getNamespaceIdentifier();
		    String keystoreProviderType = systemTypeExtensions[i].getAttribute("class"); //$NON-NLS-1$
		    
			// use the name space to get the bundle
		    Bundle bundle = Platform.getBundle(nameSpace);
		    if (bundle.getState() != Bundle.UNINSTALLED) 
		    {
		        SystemKeystoreProviderManager.getInstance().registerKeystoreProvider(bundle, keystoreProviderType);
		    }
		}
	}

    /**
     *  Return all elements that extend the org.eclipse.rse.core.subsystemConfigurations extension point
     */
    private IConfigurationElement[] getSubSystemConfigurationPlugins()
    {
   	    // Get reference to the plug-in registry
	    IExtensionRegistry registry = Platform.getExtensionRegistry();
	    // Get configured extenders
	    IConfigurationElement[] subsystemConfigurationExtensions =
		  registry.getConfigurationElementsFor("org.eclipse.rse.core","subsystemConfigurations"); //$NON-NLS-1$ //$NON-NLS-2$   	

	    return subsystemConfigurationExtensions;
    }

    /**
     * Return an array of SubSystemConfigurationProxy objects.
     * These represent all extensions to our subsystemConfigurations extension point.
     */
    public ISubSystemConfigurationProxy[] getSubSystemConfigurationProxies()
    {
    	if (_subsystemConfigurations != null) // added by PSC
    		return _subsystemConfigurations;

    	IConfigurationElement[] factoryPlugins = getSubSystemConfigurationPlugins();
    	if (factoryPlugins != null)
    	{
          List l = new ArrayList();
          for (int idx=0; idx<factoryPlugins.length; idx++)
          {
             SubSystemConfigurationProxy ssf =
               new SubSystemConfigurationProxy(factoryPlugins[idx]);           	
          	
             l.add(ssf);
          }
          ISubSystemConfigurationProxy[] newProxies = (ISubSystemConfigurationProxy[])l.toArray(new ISubSystemConfigurationProxy[l.size()]);
  		  //[149280][165674]: Sort proxies by priority then ID in order to 
          //get deterministic results on all getSubSystemConfiguration*() queries
          Arrays.sort(newProxies, new SubSystemConfigurationProxyComparator());
          _subsystemConfigurations = newProxies;
    	}
    	
    	return _subsystemConfigurations;
    }


}