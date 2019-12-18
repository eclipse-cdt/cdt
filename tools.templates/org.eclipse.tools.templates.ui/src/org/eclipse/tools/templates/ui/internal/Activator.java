/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tools.templates.ui.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "org.eclipse.tools.templates.ui"; //$NON-NLS-1$

	private static Activator plugin;

	private TemplateExtension templateExtension;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		templateExtension = new TemplateExtension();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static String getId() {
		return plugin.getBundle().getSymbolicName();
	}

	public static TemplateExtension getTemplateExtension() {
		return plugin.templateExtension;
	}

	public static void log(Exception e) {
		if (e instanceof CoreException) {
			plugin.getLog().log(((CoreException) e).getStatus());
		} else {
			plugin.getLog().log(new Status(IStatus.ERROR, getId(), e.getLocalizedMessage(), e));
		}
	}

	/**
	 * Creates an error status.
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static Status createErrorStatus(String message) {
		return createErrorStatus(message, null);
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
	public static void log(IStatus status) {
		plugin.getLog().log(status);
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void log(String message, Throwable e) {
		log(createErrorStatus(message, e));
	}

	/**
	 * Utility method with conventions
	 */
	public static void errorDialog(Shell shell, String title, String message, Throwable t, boolean logError) {
		if (logError)
			log(message, t);

		IStatus status;
		if (t instanceof CoreException) {
			status = ((CoreException) t).getStatus();
			// if the 'message' resource string and the IStatus' message are the same,
			// don't show both in the dialog
			if (status != null && message.equals(status.getMessage())) {
				message = null;
			}
		} else {
			status = new Status(IStatus.ERROR, PLUGIN_ID, -1, "Internal Error: ", t); //$NON-NLS-1$
		}
		ErrorDialog.openError(shell, title, message, status);
	}

}
