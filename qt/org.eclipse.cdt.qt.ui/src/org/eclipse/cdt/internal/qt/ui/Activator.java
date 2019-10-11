/*
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.ui;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.internal.qt.ui.resources.QtResourceChangeListener;
import org.eclipse.cdt.internal.qt.ui.resources.QtWorkspaceSaveParticipant;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.qt.ui"; //$NON-NLS-1$

	public static final String IMG_QT_16 = "qt16"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	public static Image getQtLogo() {
		return null;
	}

	public static Image getQtLogoLarge() {
		return null;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		ImageRegistry imageRegistry = getImageRegistry();
		imageRegistry.put(IMG_QT_16, ResourceLocator.imageDescriptorFromBundle(PLUGIN_ID, "icons/qt16.png").get()); //$NON-NLS-1$

		// Use a save participant to grab any changed resources while this
		// plugin was inactive
		QtResourceChangeListener resourceManager = new QtResourceChangeListener();
		ISaveParticipant saveParticipant = new QtWorkspaceSaveParticipant();
		ISavedState lastState = ResourcesPlugin.getWorkspace().addSaveParticipant(Activator.PLUGIN_ID, saveParticipant);
		if (lastState != null) {
			lastState.processResourceChangeEvents(resourceManager);
		}
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceManager);
	}

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
	public static Activator getDefault() {
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
		String msg = e.getMessage();
		if (msg == null) {
			log("Error", e); //$NON-NLS-1$
		} else {
			log("Error: " + msg, e); //$NON-NLS-1$
		}
	}

	public static void log(String message, Throwable e) {
		Throwable nestedException;
		if (e instanceof CModelException && (nestedException = ((CModelException) e).getException()) != null) {
			e = nestedException;
		}
		log(IStatus.ERROR, message, e);
	}

	public static void log(int code, String msg, Throwable e) {
		getDefault().getLog().log(new Status(code, PLUGIN_ID, msg, e));
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static <T> T getService(Class<T> service) {
		BundleContext context = plugin.getBundle().getBundleContext();
		ServiceReference<T> ref = context.getServiceReference(service);
		return ref != null ? context.getService(ref) : null;
	}

	public static Image getImage(String key) {
		return plugin.getImageRegistry().get(key);
	}

}
