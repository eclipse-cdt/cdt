/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - initial contribution
 *******************************************************************************/
package org.eclipse.remote.serial.internal.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

public class Activator extends Plugin {

	private static Plugin plugin;

	public void start(BundleContext bundleContext) throws Exception {
		plugin = this;
	}

	public void stop(BundleContext bundleContext) throws Exception {
		plugin = null;
	}

	public static void log(IStatus status) {
		plugin.getLog().log(status);
	}

	public static void log(Exception exception) {
		if (exception instanceof CoreException) {
			log(((CoreException) exception).getStatus());
		} else {
			log(new Status(IStatus.ERROR, plugin.getBundle().getSymbolicName(), exception.getLocalizedMessage(), exception));
		}
	}

}
