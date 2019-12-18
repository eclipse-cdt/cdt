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
package org.eclipse.tools.templates.freemarker.internal;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class Activator extends Plugin {

	private static Activator plugin;

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		plugin = this;
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		super.stop(bundleContext);
		plugin = null;
	}

	public static String getId() {
		return plugin.getBundle().getSymbolicName();
	}

}
