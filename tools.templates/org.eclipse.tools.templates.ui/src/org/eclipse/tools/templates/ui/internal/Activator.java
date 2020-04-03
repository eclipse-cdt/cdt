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

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator implements BundleActivator {
	public static final String PLUGIN_ID = "org.eclipse.tools.templates.ui"; //$NON-NLS-1$

	private static Activator plugin;

	private TemplateExtension templateExtension;

	@Override
	public void start(BundleContext context) throws Exception {
		plugin = this;
		templateExtension = new TemplateExtension();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
	}

	public static TemplateExtension getTemplateExtension() {
		return plugin.templateExtension;
	}

}
