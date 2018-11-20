/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

public class LRParserPlugin extends Plugin {

	public static final String PLUGIN_ID = "org.eclipse.cdt.core.lrparser"; //$NON-NLS-1$

	private static LRParserPlugin plugin;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		try {
			// shutdown code goes here
			plugin = null;
		} finally {
			super.stop(context);
		}
	}

	/**
	 * @noreference
	 */
	public static IStatus createStatus(String msg, Throwable e) {
		return new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, msg, e);
	}

	/**
	 * @noreference
	 */
	public static IStatus createStatus(Throwable e) {
		return createStatus(e.getMessage(), e);
	}

	/**
	 * @noreference
	 */
	public static void log(IStatus status) {
		plugin.getLog().log(status);
	}

	/**
	 * @noreference
	 */
	public static void logError(Throwable exception, String message) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, message, exception));
	}

	/**
	 * @noreference
	 */
	public static void logError(Throwable exception) {
		logError(exception, exception.getMessage());
	}

	/**
	 * @noreference
	 */
	public static void logError(String message) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, message));
	}

	/**
	 * @noreference
	 */
	public static void logInfo(String message) {
		log(new Status(IStatus.INFO, PLUGIN_ID, message));
	}

}
