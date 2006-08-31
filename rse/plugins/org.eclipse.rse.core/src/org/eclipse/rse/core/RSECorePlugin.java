/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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
package org.eclipse.rse.core;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.rse.core.internal.RSECoreRegistry;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.internal.persistence.RSEPersistenceManager;
import org.eclipse.rse.logging.Logger;
import org.eclipse.rse.logging.LoggerFactory;
import org.eclipse.rse.persistence.IRSEPersistenceManager;
import org.osgi.framework.BundleContext;

/**
 * The RSE core plugin class. Clients may extend this class.
 */
public class RSECorePlugin extends Plugin {

	// the shared instance
	private static RSECorePlugin plugin;
	private Logger logger = null;

	private ISystemRegistry _registry;
    private IRSEPersistenceManager         _persistenceManager = null;

    public static IRSEPersistenceManager getThePersistenceManager()
    {
    	return getDefault().getPersistenceManager();
    }
	  /**
     * @return the persistence manager used for persisting RSE profiles
     */
    public IRSEPersistenceManager getPersistenceManager()
    {
    	if (_persistenceManager == null)
    	{
    		_persistenceManager = new RSEPersistenceManager(_registry);
    	}
    	return _persistenceManager;
    }
	/**
	 * @return the local machine name
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
	 * @return the local IP address
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
	 * The constructor.
	 */
	public RSECorePlugin() {
		plugin = this;
	}
	
	public void setSystemRegistry(ISystemRegistry registry)
	{
		_registry = registry;
	}
	
	public ISystemRegistry getSystemRegistry()
	{
		return _registry;
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
	 * Returns the shared instance.
	 * @return the shared instance.
	 */
	public static RSECorePlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the RSE core registry. Clients should use this method to get the registry which
	 * is the starting point for working with the RSE framework.
	 * @return the RSE core registry.
	 */
	public IRSECoreRegistry getRegistry() {
		return RSECoreRegistry.getDefault();
	}
	
	public Logger getLogger() {
		return logger;
	}

	private void log(Throwable t) {
		String pluginId = this.getBundle().getSymbolicName();
		IStatus status = new Status(IStatus.ERROR, pluginId, 0, "Unexpected Exception", t);
		getLog().log(status);
	}
}