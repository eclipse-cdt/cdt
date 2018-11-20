/*******************************************************************************
 * Copyright (c) 2009, 2015 Wind River Systems and others.
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
 *     Jonah Graham (Kichwa Coders) - Bug 317173 - cleanup warnings
 *******************************************************************************/
package org.eclipse.cdt.dsf.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

/**
 * Convenience class to help track DSF services that a given
 * client needs to use.  This class is similar to the standard OSGI
 * org.osgi.util.tracker.ServiceTracker class, with a few differences:
 * <br>1. This class is assumed to be accessed by a single thread hence it
 * has no synchronization built in, while OSGI ServiceTracker synchronized
 * access to its data.
 * <br>2. This class is primarily designed to track multiple services of
 * different type (class), while OSGI ServiceTracker is designed to work with
 * single class type, with optional filtering options.
 * <br>3. This class uses knowledge of DSF sessions to help narrow down
 * service references.
 * <br>4. OSGI Service tracker explicitly listens to OSGI service
 * startup/shutdown events and it will clear a reference to a service as
 * soon as it's shut down.
 * Since version 2.0, this class listens to service unregister events
 * as an indication of service shutdown.  In the case of an unregister event,
 * this class will clear the reference to that service.
 * <p>
 * That said, it might be more convenient for certain types of clients to use
 * OSGI Service tracker for the additional features it provides.
 *
 * @see org.osgi.util.tracker.ServiceTracker
 *
 * @since 1.0
 */
@ConfinedToDsfExecutor("DsfSession.getSession(sessionId).getExecutor()")
public class DsfServicesTracker {

	private static String getServiceFilter(String sessionId) {
		return ("(" + IDsfService.PROP_SESSION_ID + "=" + sessionId + ")").intern(); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	}

	final private static class ServiceKey {
		private final String fClassName;
		private final String fFilter;
		private final int fHashCode;
		private final String fHashString;

		public ServiceKey(Class<?> clazz, String filter) {
			fClassName = clazz != null ? clazz.getName() : null;
			fFilter = filter;
			fHashString = 'C' + (fClassName == null ? "" : fClassName) + //$NON-NLS-1$
					'F' + (fFilter == null ? "" : fFilter); //$NON-NLS-1$
			fHashCode = fHashString.hashCode();
		}

		@Override
		public boolean equals(Object other) {
			// hashcodes are not guaranteed to be unique, but objects that are equal must have the same hashcode
			// thus we can optimize by first comparing hashcodes
			return other instanceof ServiceKey && ((((ServiceKey) other).fHashCode == this.fHashCode)
					&& (((ServiceKey) other).fHashString.equals(this.fHashString)));
		}

		@Override
		public int hashCode() {
			return fHashCode;
		}
	}

	private final String fSessionId;
	private volatile boolean fDisposed = false;
	private final BundleContext fBundleContext;
	private final Map<ServiceKey, ServiceReference<?>> fServiceReferences = new HashMap<>();
	private final Map<ServiceReference<?>, Object> fServices = new HashMap<>();
	private final String fServiceFilter;

	private final ServiceListener fListner = new ServiceListener() {
		@Override
		public void serviceChanged(final ServiceEvent event) {
			// Only listen to unregister events.
			if (event.getType() != ServiceEvent.UNREGISTERING) {
				return;
			}

			// If session is not active anymore, just exit.  The tracker should
			// soon be disposed.
			DsfSession session = DsfSession.getSession(fSessionId);
			if (session == null) {
				return;
			}

			if (session.getExecutor().isInExecutorThread()) {
				handleUnregisterEvent(event);
			} else {
				try {
					session.getExecutor().execute(new DsfRunnable() {
						@Override
						public void run() {
							handleUnregisterEvent(event);
						};
					});
				} catch (RejectedExecutionException e) {
					// Same situation as when the session is not active
				}
			}
		}
	};

	private void handleUnregisterEvent(ServiceEvent event) {
		for (Iterator<Map.Entry<ServiceKey, ServiceReference<?>>> itr = fServiceReferences.entrySet().iterator(); itr
				.hasNext();) {
			Map.Entry<ServiceKey, ServiceReference<?>> entry = itr.next();
			if (entry.getValue().equals(event.getServiceReference())) {
				itr.remove();
			}
		}
		if (fServices.remove(event.getServiceReference()) != null) {
			fBundleContext.ungetService(event.getServiceReference());
		}
	}

	/**
	 * Only constructor.
	 * @param bundleContext Context of the plugin that the client lives in.
	 * @param sessionId The DSF session that this tracker will be used for.
	 */
	@ThreadSafe
	public DsfServicesTracker(BundleContext bundleContext, String sessionId) {
		fSessionId = sessionId;
		fBundleContext = bundleContext;
		fServiceFilter = getServiceFilter(sessionId);
		try {
			fBundleContext.addServiceListener(fListner, fServiceFilter);
		} catch (InvalidSyntaxException e) {
			assert false : "Invalid session ID syntax"; //$NON-NLS-1$
		}
	}

	/**
	 * Retrieves a service reference for given service class and optional filter.
	 * Filter should be used if there are multiple instances of the desired service
	 * running within the same session.
	 * @param serviceClass class of the desired service
	 * @param custom filter to use when searching for the service, this filter will
	 * be used instead of the standard filter so it should also specify the desired
	 * session-ID
	 * @return OSGI service reference object to the desired service, null if not found
	 */
	public <V> ServiceReference<V> getServiceReference(Class<V> serviceClass, String filter) {
		if (fDisposed) {
			return null;
		}

		// If the session is not active, all of its services are gone.
		DsfSession session = DsfSession.getSession(fSessionId);
		if (session == null) {
			return null;
		}
		assert session.getExecutor().isInExecutorThread();

		ServiceKey key = new ServiceKey(serviceClass, filter != null ? filter : fServiceFilter);
		if (fServiceReferences.containsKey(key)) {
			@SuppressWarnings("unchecked")
			ServiceReference<V> ref = (ServiceReference<V>) fServiceReferences.get(key);
			return ref;
		}

		try {
			Collection<ServiceReference<V>> references = fBundleContext.getServiceReferences(serviceClass, key.fFilter);
			assert references == null || references.size() <= 1;
			if (references == null || references.isEmpty()) {
				return null;
			} else {
				ServiceReference<V> ref = references.iterator().next();
				fServiceReferences.put(key, ref);
				return ref;
			}
		} catch (InvalidSyntaxException e) {
			assert false : "Invalid session ID syntax"; //$NON-NLS-1$
		} catch (IllegalStateException e) {
			// Can occur when plugin is shutting down.
		}
		return null;
	}

	/**
	 * Convenience class to retrieve a service based on class name only.
	 * @param serviceClass class of the desired service
	 * @return instance of the desired service, null if not found
	 */
	public <V> V getService(Class<V> serviceClass) {
		return getService(serviceClass, null);
	}

	/**
	 * Retrieves the service given service class and optional filter.
	 * Filter should be used if there are multiple instances of the desired service
	 * running within the same session.
	 * @param serviceClass class of the desired service
	 * @param custom filter to use when searching for the service, this filter will
	 * be used instead of the standard filter so it should also specify the desired
	 * session-ID
	 * @return instance of the desired service, null if not found
	 */
	public <V> V getService(Class<V> serviceClass, String filter) {
		ServiceReference<V> serviceRef = getServiceReference(serviceClass, filter);
		if (serviceRef == null) {
			return null;
		}

		@SuppressWarnings("unchecked")
		V service = (V) fServices.get(serviceRef);
		if (service == null) {
			// Check to see if 'null' means we never fetched the
			// service, or if there simply is none.
			// If we never fetched it, do so now and store the result.
			if (!fServices.containsKey(serviceRef)) {
				service = fBundleContext.getService(serviceRef);
				fServices.put(serviceRef, service);
			}
		}
		return service;
	}

	/**
	 * Un-gets all the references held by this tracker.  Must be called
	 * to avoid leaking OSGI service references.
	 */
	@ThreadSafe
	public void dispose() {
		assert !fDisposed;
		fDisposed = true;

		DsfSession session = DsfSession.getSession(fSessionId);
		if (session != null) {
			try {
				if (!session.getExecutor().isInExecutorThread()) {
					session.getExecutor().execute(new DsfRunnable() {
						@Override
						public void run() {
							doDispose();
						}
					});
					return;
				}
			} catch (RejectedExecutionException e) {
			}
		}
		// We should get to this point if
		// 1) we're in session's executor thread
		// 2) session is disposed already
		// 3) executor rejected our runnable
		// In all cases dispose the tracker in current thread.
		doDispose();
	}

	private void doDispose() {
		try {
			fBundleContext.removeServiceListener(fListner);
			for (Iterator<ServiceReference<?>> itr = fServices.keySet().iterator(); itr.hasNext();) {
				fBundleContext.ungetService(itr.next());
			}
		} catch (IllegalStateException e) {
			// May be thrown during shutdown (bug 293049).
		}
		fServices.clear();
		fServiceReferences.clear();
	}

	@Override
	protected void finalize() throws Throwable {
		assert fDisposed;
		super.finalize();
	}
}
