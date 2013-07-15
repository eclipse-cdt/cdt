/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.internal.remote.ui.RemoteUIPlugin;
import org.eclipse.internal.remote.ui.RemoteUIServicesProxy;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.RemoteServices;
import org.eclipse.ui.PlatformUI;

/**
 * Main entry point for accessing remote UI services.
 * 
 * @since 7.0
 */
public class RemoteUIServices {
	private static final String EXTENSION_POINT_ID = "remoteUIServices"; //$NON-NLS-1$

	private static Map<String, RemoteUIServicesProxy> fRemoteUIServices = null;

	/**
	 * Look up a remote service provider and ensure it is initialized. The method will use the supplied container's progress
	 * service, or, if null, the platform progress service, in order to allow the initialization to be canceled.
	 * 
	 * @param id
	 *            id of service to locate
	 * @param context
	 *            context with progress service, or null to use the platform progress service
	 * @return remote service or null if the service can't be located or the progress monitor was canceled
	 * @since 5.0
	 */
	public static IRemoteServices getRemoteServices(final String id, IRunnableContext context) {
		final IRemoteServices[] remoteService = new IRemoteServices[1];
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				remoteService[0] = RemoteServices.getRemoteServices(id, monitor);
			}
		};
		try {
			if (context != null) {
				context.run(true, false, runnable);
			} else {
				PlatformUI.getWorkbench().getProgressService().busyCursorWhile(runnable);
			}
		} catch (InvocationTargetException e) {
			// Ignored
		} catch (InterruptedException e) {
			// cancelled
		}

		return remoteService[0];
	}

	/**
	 * Helper method to find UI services that correspond to a particular remote services implementation
	 * 
	 * @param services
	 * @return remote UI services
	 */
	public static IRemoteUIServices getRemoteUIServices(IRemoteServices services) {
		if (fRemoteUIServices == null) {
			fRemoteUIServices = retrieveRemoteUIServices();
		}

		/*
		 * Find the UI services corresponding to services.
		 */
		RemoteUIServicesProxy proxy = fRemoteUIServices.get(services.getId());
		if (proxy != null) {
			return proxy.getUIServices(services);
		}
		return null;
	}

	/**
	 * Find and load all remoteUIServices plugins.
	 */
	private static Map<String, RemoteUIServicesProxy> retrieveRemoteUIServices() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(RemoteUIPlugin.getUniqueIdentifier(), EXTENSION_POINT_ID);
		final IExtension[] extensions = extensionPoint.getExtensions();

		Map<String, RemoteUIServicesProxy> services = new HashMap<String, RemoteUIServicesProxy>(5);

		for (IExtension ext : extensions) {
			final IConfigurationElement[] elements = ext.getConfigurationElements();

			for (IConfigurationElement ce : elements) {
				RemoteUIServicesProxy proxy = new RemoteUIServicesProxy(ce);
				services.put(proxy.getId(), proxy);
			}
		}

		return services;
	}
}
