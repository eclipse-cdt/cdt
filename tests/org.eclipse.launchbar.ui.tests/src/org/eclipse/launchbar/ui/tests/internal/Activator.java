/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.launchbar.ui.tests.internal;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Activator extends AbstractUIPlugin {

	private static BundleContext bundleContext;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		bundleContext = context;
	}

	public static <T> T getService(Class<T> service) {
		ServiceReference<T> ref = bundleContext.getServiceReference(service);
		return ref != null ? bundleContext.getService(ref) : null;
	}

}
