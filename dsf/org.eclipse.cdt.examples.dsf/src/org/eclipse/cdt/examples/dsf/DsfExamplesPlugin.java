/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf;

import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class DsfExamplesPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.examples.dsf"; //$NON-NLS-1$

	public static final String IMG_LAYOUT_TOGGLE = "icons/layout.gif"; //$NON-NLS-1$
	public static final String IMG_ALARM = "icons/alarm.gif"; //$NON-NLS-1$
	public static final String IMG_ALARM_TRIGGERED = "icons/alarm_triggered.gif"; //$NON-NLS-1$
	public static final String IMG_TIMER = "icons/timer.gif"; //$NON-NLS-1$
	public static final String IMG_REMOVE = "icons/remove.gif"; //$NON-NLS-1$

	// The shared instance
	private static DsfExamplesPlugin fgPlugin;

	private static BundleContext fgBundleContext;

	/**
	 * The constructor
	 */
	public DsfExamplesPlugin() {
		fgPlugin = this;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		fgBundleContext = context;
		super.start(context);
		getImageRegistry().put(IMG_ALARM, ResourceLocator.imageDescriptorFromBundle(PLUGIN_ID, IMG_ALARM).get());
		getImageRegistry().put(IMG_ALARM_TRIGGERED,
				ResourceLocator.imageDescriptorFromBundle(PLUGIN_ID, IMG_ALARM_TRIGGERED).get());
		getImageRegistry().put(IMG_TIMER, ResourceLocator.imageDescriptorFromBundle(PLUGIN_ID, IMG_TIMER).get());
		getImageRegistry().put(IMG_REMOVE, ResourceLocator.imageDescriptorFromBundle(PLUGIN_ID, IMG_REMOVE).get());
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		fgPlugin = null;
		fgBundleContext = null;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static DsfExamplesPlugin getDefault() {
		return fgPlugin;
	}

	public static BundleContext getBundleContext() {
		return fgBundleContext;
	}

}
