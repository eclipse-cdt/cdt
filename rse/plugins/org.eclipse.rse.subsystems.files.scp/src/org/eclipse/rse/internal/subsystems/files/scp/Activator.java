/*******************************************************************************
 * Copyright (c) 2009, 2010 Mentor Graphics Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Nikita Shulga (Mentor Graphics) - initial implementation 
 *******************************************************************************/

package org.eclipse.rse.internal.subsystems.files.scp;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = Activator.class.getCanonicalName();

	private static Activator plugin = null;
	private static Bundle bundle = null;

	public Activator() {
	}

	public static Plugin getDefault() {
		return plugin;
	}

	public static Bundle getDefaultBundle() {
		return bundle;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		bundle = context.getBundle();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		bundle = null;
		super.stop(context);
	}

	public static void log(String msg) {
		log(msg, null);
	}

	public static void log(String msg, Exception e) {
		log(IStatus.INFO, msg, e);
	}

	public static void warn(String msg, Exception e) {
		log(IStatus.WARNING, msg, e);
	}

	public static void log(int sev, String msg, Exception e) {
		Platform.getLog(getDefaultBundle()).log(
				new Status(sev, PLUGIN_ID, msg, e));
	}

}
