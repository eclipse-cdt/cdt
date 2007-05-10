/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others. All rights reserved.
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
 ********************************************************************************/
package org.eclipse.rse.core;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.internal.core.RSECoreRegistry;
import org.eclipse.rse.internal.persistence.RSEPersistenceManager;
import org.eclipse.rse.logging.Logger;
import org.eclipse.rse.logging.LoggerFactory;
import org.eclipse.rse.persistence.IRSEPersistenceManager;
import org.osgi.framework.BundleContext;

/**
 * RSECorePlugin provides the activation for the RSE core and acts as the primary
 * registry for logging, persistence, and the main RSE service registries.
 * It should not be extended by other classes.
 */
public class RSECorePlugin extends Plugin {

	/**
	 * Current release as a number (multiplied by 10). E.g. 30 is for release 3.0.
	 */
	public static final int CURRENT_RELEASE = 100; // updated to new release

	/**
	 * Current release as a string.
	 */
	public static final String CURRENT_RELEASE_NAME = "1.0.0";  //$NON-NLS-1$

	private static RSECorePlugin plugin = null; // the singleton instance of this plugin
	private Logger logger = null;
	private ISystemRegistry _registry = null;
	private IRSEPersistenceManager _persistenceManager = null;

	/**
	 * Returns the singleton instance of RSECorePlugin.
	 * @return the singleton instance.
	 */
	public static RSECorePlugin getDefault() {
		return plugin;
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
	 * A static convenience method - fully equivalent to 
	 * <code>RSECorePlugin.getDefault().getRegistry()</code>.
	 * @return the RSE Core Registry.
	 */
	public static IRSECoreRegistry getTheCoreRegistry() {
		return getDefault().getCoreRegistry();
	}
	
	/**
	 * A static convenience method - fully equivalent to 
	 * <code>RSECorePlugin.getDefault().getSystemRegistry()</code>.
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
	 * Returns a qualified hostname given a potentially unqualified hostname
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
		logger = LoggerFactory.getLogger(this);
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
			_persistenceManager = new RSEPersistenceManager(_registry);
		}
		return _persistenceManager;
	}

	/**
	 * Sets the system registry. This is the main registry that can be used by RSE components
	 * that require a user interface. This should be set only by RSE startup components and 
	 * not by any external client.
	 * @param registry the implementation of ISystemRegistry that the core should remember.
	 */
	public void setSystemRegistry(ISystemRegistry registry) {
		_registry = registry;
	}

	/**
	 * Gets the system registry set by {@link #setSystemRegistry(ISystemRegistry)}.
	 * This registry is used to gain access to the basic services and components used in 
	 * the RSE user interface. 
	 * @return the RSE system registry
	 */
	public ISystemRegistry getSystemRegistry() {
		return _registry;
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
		return logger;
	}
	
	/**
	 * Log an unexpected exception that occurs during the functioning of this class.
	 * @param t the exception to log
	 */
	private void log(Throwable t) {
		getLogger().logError("Unexpected Exception", t); //$NON-NLS-1$
	}
}