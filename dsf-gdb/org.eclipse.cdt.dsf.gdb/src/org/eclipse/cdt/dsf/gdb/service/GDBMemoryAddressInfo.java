/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.Arrays;
import java.util.Hashtable;

import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.internal.memory.GdbMemoryAddressInfoVariablesRetrieval;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.osgi.framework.BundleContext;

/**
 * The service in charge to provide the set of available IGdbMemoryAddressInfoTypeRetrieval providers
 * @since 5.0
 */
public class GDBMemoryAddressInfo extends AbstractDsfService implements IMemoryAddressInfo {

	private final IGdbMemoryAddressInfoTypeRetrieval[] fInfoTypeProviders;
	public GDBMemoryAddressInfo(DsfSession session) {
		super(session);
        fInfoTypeProviders = new IGdbMemoryAddressInfoTypeRetrieval[] {
                new GdbMemoryAddressInfoVariablesRetrieval(getSession()) };
	}

	@Override
	public void initialize(final RequestMonitor requestMonitor) {
		super.initialize(new ImmediateRequestMonitor(requestMonitor) {
			@Override
			public void handleSuccess() {
				doInitialize(requestMonitor);
			}
		});
	}

	private void doInitialize(final RequestMonitor requestMonitor) {
		register(new String[] {
				IMemoryAddressInfo.class.getName(),
				GDBMemoryAddressInfo.class.getName() },
				new Hashtable<String, String>());
		requestMonitor.done();
	}

	@Override
	public IGdbMemoryAddressInfoTypeRetrieval[] getMemoryAddressInfoProviders() {
		return Arrays.copyOf(fInfoTypeProviders, fInfoTypeProviders.length);
	}

	@Override
	protected BundleContext getBundleContext() {
		return GdbPlugin.getBundleContext();
	}

}
