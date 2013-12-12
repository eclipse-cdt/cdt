/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.qt.ui;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class QtUIPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.qt.ui"; //$NON-NLS-1$

	// The shared instance
	private static QtUIPlugin plugin;

	/**
	 * The constructor
	 */
	public QtUIPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static QtUIPlugin getDefault() {
		return plugin;
	}

    public static CoreException coreException(String msg) {
    	return new CoreException(new Status(IStatus.INFO, PLUGIN_ID, msg));
    }

	public static IStatus info(String msg) {
		return new Status(IStatus.INFO, PLUGIN_ID, msg);
	}

	public static IStatus error(String msg) {
		return error(msg, null);
	}

	public static IStatus error(String msg, Throwable e) {
		return new Status(IStatus.ERROR, PLUGIN_ID, msg, e);
	}

	public static void log(String e) {
		log(IStatus.INFO, e, null);
	}

	public static void log(Throwable e) {
		String msg= e.getMessage();
		if (msg == null) {
			log("Error", e); //$NON-NLS-1$
		} else {
			log("Error: " + msg, e); //$NON-NLS-1$
		}
	}

	public static void log(String message, Throwable e) {
		Throwable nestedException;
		if (e instanceof CModelException
				&& (nestedException = ((CModelException)e).getException()) != null) {
			e = nestedException;
		}
		log(IStatus.ERROR, message, e);
	}

	public static void log(int code, String msg, Throwable e) {
		getDefault().getLog().log(new Status(code, PLUGIN_ID, msg, e));
	}
}
