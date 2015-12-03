package org.eclipse.cdt.qt.core.tests;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Activator implements BundleActivator {

	private static BundleContext context;

	@Override
	public void start(BundleContext context) throws Exception {
		Activator.context = context;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		Activator.context = null;
	}

	public static Bundle getBundle() {
		return context != null ? context.getBundle() : null;
	}

	public static <T> T getService(Class<T> service) {
		ServiceReference<T> ref = context.getServiceReference(service);
		return ref != null ? context.getService(ref) : null;
	}

}
