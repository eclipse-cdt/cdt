/*******************************************************************************
 * Copyright (c) 2011, 2018 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.ssh.activator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jsch.core.IJSchService;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tm.terminal.connector.ssh.connector.SshConnection;
import org.eclipse.tm.terminal.view.core.tracing.TraceHandler;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class UIPlugin extends AbstractUIPlugin {
	// The shared instance
	private static UIPlugin plugin;
	// The trace handler instance
	private static volatile TraceHandler traceHandler;

	// ServiceTracker for IJschService
	private ServiceTracker tracker;

	/**
	 * The constructor
	 */
	public UIPlugin() {
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static UIPlugin getDefault() {
		return plugin;
	}

	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 */
	public static String getUniqueIdentifier() {
		if (getDefault() != null && getDefault().getBundle() != null) {
			return getDefault().getBundle().getSymbolicName();
		}
		return "org.eclipse.tm.terminal.connector.ssh"; //$NON-NLS-1$
	}

	/**
	 * Returns the bundles trace handler.
	 *
	 * @return The bundles trace handler.
	 */
	public static TraceHandler getTraceHandler() {
		if (traceHandler == null) {
			traceHandler = new TraceHandler(getUniqueIdentifier());
		}
		return traceHandler;
	}

	//---------------------------------------------------------------------------
	//<copied code from org.eclipse.team.cvs.ssh2/CVSSSH2Plugin (Copyright IBM)>
	//---------------------------------------------------------------------------

	/* (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		tracker = new ServiceTracker(getBundle().getBundleContext(), IJSchService.class.getName(), null);
		tracker.open();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		try {
			SshConnection.shutdown();
			tracker.close();
		} finally {
			plugin = null;
			super.stop(context);
		}
	}

	/**
	 * Returns an instance of IJSchService from the OSGi Registry.
	 * @return An instance of IJSchService, or <code>null</code> if no
	 * 		IJschService service is available.
	 */
	public IJSchService getJSchService() {
		return (IJSchService) tracker.getService();
	}

	//---------------------------------------------------------------------------
	//<copied code from org.eclipse.team.cvs.ssh2/CVSSSH2Plugin (Copyright IBM)>
	//---------------------------------------------------------------------------

	/* (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeImageRegistry(org.eclipse.jface.resource.ImageRegistry)
	 */
	@Override
	protected void initializeImageRegistry(ImageRegistry registry) {
	}

	/**
	 * Loads the image registered under the specified key from the image
	 * registry and returns the <code>Image</code> object instance.
	 *
	 * @param key The key the image is registered with.
	 * @return The <code>Image</code> object instance or <code>null</code>.
	 */
	public static Image getImage(String key) {
		return getDefault().getImageRegistry().get(key);
	}

	/**
	 * Loads the image registered under the specified key from the image
	 * registry and returns the <code>ImageDescriptor</code> object instance.
	 *
	 * @param key The key the image is registered with.
	 * @return The <code>ImageDescriptor</code> object instance or <code>null</code>.
	 */
	public static ImageDescriptor getImageDescriptor(String key) {
		return getDefault().getImageRegistry().getDescriptor(key);
	}
}
