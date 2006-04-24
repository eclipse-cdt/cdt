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


package org.eclipse.rse.dstore.security;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class UniversalSecurityPlugin extends AbstractUIPlugin
{
	private final static String KEYSTORE = "dstorekeystore.dat";
	private static UniversalSecurityPlugin inst;
	private static ResourceBundle aResourceBundle;
	public static final String PLUGIN_ID = "org.eclipse.rse.dstore.security"; //$NON-NLS-1$

	public UniversalSecurityPlugin() {
		if (inst == null)
			inst = this;
	}
	
	public static UniversalSecurityPlugin getDefault() {
		return inst;
	}

	public static String getPluginId() {
		return PLUGIN_ID;
	}

	public static ResourceBundle getResourceBundle() {
		if(aResourceBundle == null)
		{
			try {
				aResourceBundle = Platform.getResourceBundle(Platform.getBundle(PLUGIN_ID));
			} catch (Exception e) {
				aResourceBundle = null;
			}
		}
		return aResourceBundle;
	}
	
	public static String getString(String key) {
		try {
			return getResourceBundle().getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}
	
	

	public static String getKeyStoreLocation() {
		
		Bundle bundle = Platform.getBundle(PLUGIN_ID);
		return Platform.getStateLocation(bundle).append(KEYSTORE).toOSString();
	}

	public static String getKeyStorePassword()
	{
		return "dstore";
	}
	
	public static String getWorkspaceName(){
		IPath workspace = Platform.getLocation();
		int nr = workspace.segmentCount();
		String workspaceName = workspace.segment(nr - 1);
		return workspaceName;		
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {

		super.stop(context);
		
		savePluginPreferences();
		ImageRegistry.shutdown();
	}

	public static Shell getActiveWorkbenchShell() {
		return getActiveWorkbenchWindow().getShell();
	}
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}
	
	public void log(IStatus status) {
		getLog().log(status);
	}
	
	public void log(Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, "Error", e)); //$NON-NLS-1$
	}
	
}