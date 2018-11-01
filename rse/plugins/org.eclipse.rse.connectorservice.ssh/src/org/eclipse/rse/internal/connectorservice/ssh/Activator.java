/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License 2.0 
 * which accompanies this distribution, and is available at 
 * https://www.eclipse.org/legal/epl-2.0/ 
 * 
 * Contributors: 
 * Martin Oberhuber (Wind River) - initial API and implementation 
 * Martin Oberhuber (Wind River) - [175686] Adapted to new IJSchService API 
 *    - copied code from org.eclipse.team.cvs.ssh2/CVSSSH2Plugin (Copyright IBM)
 *******************************************************************************/
package org.eclipse.rse.internal.connectorservice.ssh;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jsch.core.IJSchService;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.eclipse.rse.connectorservice.ssh"; //$NON-NLS-1$
	private static Activator plugin;

	// ServiceTracker for IJschService
	private ServiceTracker tracker;

	/**
	 * The constructor
	 */
	public Activator() {
		super();
		plugin = this;
	}

    //---------------------------------------------------------------------------
	//<copied code from org.eclipse.team.cvs.ssh2/CVSSSH2Plugin (Copyright IBM)>
    //---------------------------------------------------------------------------

	public void start(BundleContext context) throws Exception {
		super.start(context);
	    tracker = new ServiceTracker(getBundle().getBundleContext(), IJSchService.class.getName(), null);
	    tracker.open();
	}

	public void stop(BundleContext context) throws Exception {
		try {
			SshConnectorService.shutdown();
			tracker.close();
		} finally {
			plugin = null;
			super.stop(context);
		}
	}

	/**
	 * Returns an instance of IJSchService from the OSGi Registry.
	 * @return An instance of IJSchService, or <code>null</code> if no 
	 * 		IJschService service is available.
	 */
    public IJSchService getJSchService() {
        return (IJSchService)tracker.getService();
    }

    //---------------------------------------------------------------------------
	//</copied code from org.eclipse.team.cvs.ssh2/CVSSSH2Plugin (Copyright IBM)>
    //---------------------------------------------------------------------------
    
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	//<tracing code>----------------------------------------------------

	private static Boolean fTracingOn = null;
	public static boolean isTracingOn() {
		if (fTracingOn==null) {
			String id = plugin.getBundle().getSymbolicName();
			String val = Platform.getDebugOption(id + "/debug"); //$NON-NLS-1$
			if ("true".equals(val)) { //$NON-NLS-1$
				fTracingOn = Boolean.TRUE;
			} else {
				fTracingOn = Boolean.FALSE;
			}
		}
		return fTracingOn.booleanValue();
	}
	public static String getTimestamp() {
		try {
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"); //$NON-NLS-1$
			return formatter.format(new Date());
		} catch (Exception e) {
			// If there were problems writing out the date, ignore and
			// continue since that shouldn't stop us from logging the rest
			// of the information
		}
		return Long.toString(System.currentTimeMillis());
	}
	public static void trace(String msg) {
		if (isTracingOn()) {
			String fullMsg = getTimestamp() + " | " + Thread.currentThread().getName() + " | " + msg; //$NON-NLS-1$ //$NON-NLS-2$
			System.out.println(fullMsg);
			System.out.flush();
		}
	}

	//</tracing code>---------------------------------------------------

}
