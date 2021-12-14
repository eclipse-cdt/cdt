/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.core.tests.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Activator implements BundleActivator {

	private static BundleContext bundleContext;

	@Override
	public void start(BundleContext context) throws Exception {
		bundleContext = context;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		bundleContext = null;
	}

	public static <T> T getService(Class<T> service) {
		ServiceReference<T> ref = bundleContext.getServiceReference(service);
		return ref != null ? bundleContext.getService(ref) : null;
	}

}
