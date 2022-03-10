/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation, and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - initial contribution
 *******************************************************************************/
package org.eclipse.remote.telnet.internal.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

public class Activator extends Plugin {
	public static final String PLUGIN_ID = "org.eclipse.remote.telnet.core"; //$NON-NLS-1$

	private static Plugin plugin;

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		plugin = this;
		Logger.configure(bundleContext);
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		plugin = null;
	}

	public static Plugin getDefault() {
		return plugin;
	}

	public static void log(IStatus status) {
		plugin.getLog().log(status);
	}

	public static void log(Throwable e) {
		if (e instanceof CoreException) {
			log(((CoreException) e).getStatus());
		} else {
			log(new Status(IStatus.ERROR, plugin.getBundle().getSymbolicName(), IStatus.ERROR, e.getLocalizedMessage(),
					e));
		}
	}

}
