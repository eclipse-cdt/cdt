/*******************************************************************************
 * Copyright (c) 2006, 2010 PalmSource, Inc.and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Ewa Matejska    (PalmSource)       
 * Anna Dushistova (Mentor Graphics) - [314659] move remote launch/debug to DSF 
 *******************************************************************************/

package org.eclipse.cdt.internal.launch.remote;

import java.util.HashSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.debug.core.ILaunchManager;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.launch.remote"; //$NON-NLS-1$


	private static final String REMOTE_LAUNCH_TYPE = "org.eclipse.rse.remotecdt.RemoteApplicationLaunch"; //$NON-NLS-1$


	private static final String PREFERRED_DEBUG_REMOTE_LAUNCH_DELEGATE = "org.eclipse.rse.remotecdt.dsf.debug"; //$NON-NLS-1$

	
	/* The shared instance */
	private static Activator plugin;
	
	/**
	 * The constructor.
	 */
	public Activator() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		setDefaultLaunchDelegates();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 *
	 * @return the shared instance.
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public static BundleContext getBundleContext() {
		return getDefault().getBundle().getBundleContext();
	}

	private void setDefaultLaunchDelegates() {
		// Set the default launch delegates as early as possible, and do it only once (Bug 312997) 
		ILaunchManager launchMgr = DebugPlugin.getDefault().getLaunchManager();

		HashSet<String> debugSet = new HashSet<String>();
		debugSet.add(ILaunchManager.DEBUG_MODE);

		ILaunchConfigurationType remoteCfg = launchMgr.getLaunchConfigurationType(REMOTE_LAUNCH_TYPE);
		try {
			if (remoteCfg.getPreferredDelegate(debugSet) == null) {
				ILaunchDelegate[] delegates = remoteCfg.getDelegates(debugSet);
				for (ILaunchDelegate delegate : delegates) {
					if (PREFERRED_DEBUG_REMOTE_LAUNCH_DELEGATE.equals(delegate.getId())) {
						remoteCfg.setPreferredDelegate(debugSet, delegate);
						break;
					}
				}
			}
		} catch (CoreException e) {}
	}
	

}
