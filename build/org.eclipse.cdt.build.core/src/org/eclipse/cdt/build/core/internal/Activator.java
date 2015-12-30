/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.build.core.internal;

import org.eclipse.cdt.build.core.IBuildConfigurationManager;
import org.eclipse.cdt.build.core.IToolChainManager;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Activator extends Plugin {

	private static Activator plugin;

	private static ToolChainManager toolChainManager;
	private static CBuildConfigurationManager buildConfigManager;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		toolChainManager = new ToolChainManager();
		context.registerService(IToolChainManager.class, toolChainManager, null);

		buildConfigManager = new CBuildConfigurationManager();
		context.registerService(IBuildConfigurationManager.class, buildConfigManager, null);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(buildConfigManager);

		// Save participant for toolchain data
		ResourcesPlugin.getWorkspace().addSaveParticipant(getId(), new ScannerInfoSaveParticipant());
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;

		toolChainManager = null;

		ResourcesPlugin.getWorkspace().removeResourceChangeListener(buildConfigManager);
		buildConfigManager = null;

		super.stop(context);
	}

	public static Activator getDefault() {
		return plugin;
	}

	public static String getId() {
		return plugin.getBundle().getSymbolicName();
	}

	public static ToolChainManager getToolChainManager() {
		return toolChainManager;
	}

	public static void log(IStatus status) {
		plugin.getLog().log(status);
	}

	public static void log(Exception e) {
		if (e instanceof CoreException) {
			plugin.getLog().log(((CoreException) e).getStatus());
		} else {
			plugin.getLog().log(new Status(IStatus.ERROR, getId(), e.getLocalizedMessage(), e));
		}
	}

	public static <T> T getService(Class<T> service) {
		BundleContext context = plugin.getBundle().getBundleContext();
		ServiceReference<T> ref = context.getServiceReference(service);
		return ref != null ? context.getService(ref) : null;
	}

}
