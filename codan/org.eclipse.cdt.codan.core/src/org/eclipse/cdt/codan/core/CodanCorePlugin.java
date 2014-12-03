/*******************************************************************************
 * Copyright (c) 2009, 2010 Alena Laskavaia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core;

import org.eclipse.cdt.codan.internal.core.CodeAnalysisNature;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class CodanCorePlugin extends Plugin {
	/** The plug-in ID */
	public static final String PLUGIN_ID = "org.eclipse.cdt.codan.core"; //$NON-NLS-1$
	/**
	 * The nature used to run Codan builder.
	 * @noreference This constant is not intended to be referenced by clients.
	 */
	public static final String NATURE_ID = CodeAnalysisNature.NATURE_ID;
	// The shared instance
	private static CodanCorePlugin plugin;

	/**
	 * The constructor
	 */
	public CodanCorePlugin() {
	}

	/**
	 * @return the preferences node for this plug-in.
	 */
	public IEclipsePreferences getStorePreferences() {
		return InstanceScope.INSTANCE.getNode(PLUGIN_ID);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
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
	public static CodanCorePlugin getDefault() {
		return plugin;
	}

	/**
	 * Logs the specified status with this plug-in's log.
	 *
	 * @param status
	 *        status to log
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	/**
	 * Logs an internal error with the specified throwable
	 *
	 * @param e the exception to be logged
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void log(Throwable e) {
		if (Boolean.valueOf(System.getProperty("codan.rethrow"))) //$NON-NLS-1$
			throw new RuntimeException(e);
		log("Internal Error", e); //$NON-NLS-1$
	}

	/**
	 * Logs an internal error with the specified message and throwable
	 *
	 * @param message the error message to log
	 * @param e the exception to be logged
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void log(String message, Throwable e) {
		log(new Status(IStatus.ERROR, CodanCorePlugin.PLUGIN_ID, 1, message, e));
	}

	/**
	 * Logs an internal error with the specified message.
	 *
	 * @param message the error message to log
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void log(String message) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, 1, message, null));
	}
}
