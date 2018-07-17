/*******************************************************************************
 * Copyright (c) 2018 Manish Khurana , Nathan Ridge and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.lsp4e.cpp.language;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

	public Activator() {
	}

	public static final String PLUGIN_ID = "org.eclipse.lsp4e.cpp.language"; //$NON-NLS-1$

	private static Activator plugin;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Creates an error status.
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static Status createErrorStatus(String message, Throwable e) {
		return new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, message, e);
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void log(Throwable e) {
		log("Error", e); //$NON-NLS-1$
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void log(String message, Throwable e) {
		log(createErrorStatus(message, e));
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
}
