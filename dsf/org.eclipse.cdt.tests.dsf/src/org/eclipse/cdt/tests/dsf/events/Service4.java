/*******************************************************************************
 * Copyright (c) 2009 Freescale Semiconductor and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Freescale Semiconductor - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.events;

import java.util.Hashtable;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.junit.Assert;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;

/**
 * This service differs from the other three in that when it registers itself as
 * an event listener with the dsf session, it specifies a services filter.
 *
 */
public class Service4 extends AbstractService {
	Service4(DsfSession session) {
		super(session);
	}

	@Override
	public void initialize(final RequestMonitor requestMonitor) {
		super.initialize(new RequestMonitor(getExecutor(), requestMonitor) {
			@Override
			public void handleSuccess() {
				doInitialize(requestMonitor);
			}
		});
	}

	private void doInitialize(RequestMonitor requestMonitor) {
		getServicesTracker().getService(Service1.class);
		getServicesTracker().getService(Service2.class);
		getServicesTracker().getService(Service3.class);
		register(new String[] { Service4.class.getName() }, new Hashtable<String, String>());
		requestMonitor.done();
	}

	@Override
	public void shutdown(RequestMonitor requestMonitor) {
		unregister();
		super.shutdown(requestMonitor);
	}

	/**
	 * We want to get events only from Service2.
	 * @see org.eclipse.cdt.tests.dsf.events.AbstractService#getEventServicesFilter()
	 */
	@Override
	protected Filter getEventServicesFilter() {
		try {
			return getBundleContext().createFilter("(objectClass=org.eclipse.cdt.tests.dsf.events.Service2)");
		} catch (InvalidSyntaxException e) {
			Assert.fail();
			return null;
		}
	}
}
