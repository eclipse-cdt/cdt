/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Martin Oberhuber (Wind River) - initial API and implementation 
 *******************************************************************************/

package org.eclipse.rse.services.ssh;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.BundleContext;


/**
 * The main plugin class to be used in the desktop.
 */
public class Activator extends Plugin {

	//The shared instance.
	private static Activator plugin;
	
	/**
	 * The constructor.
	 */
	public Activator() {
		super();
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
//		 make sure files.ui is activated
//		org.eclipse.rse.files.ui.Activator.getDefault();
	}

	/**
	 * This method is called when the plug-in is stopped
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

	/**
	 * Logs an throwable to the log for this plugin.
	 * @param t the Throwable to be logged.
	 */
	public void logException(Throwable t) {
		ILog log = getLog();
		String id = getBundle().getSymbolicName();
		String message = NLS.bind(SshServiceResources.SshPlugin_Unexpected_Exception, t.getClass().getName(), t.getLocalizedMessage());
		IStatus status = new Status(IStatus.ERROR, id, 0, message, t);
		log.log(status);
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
