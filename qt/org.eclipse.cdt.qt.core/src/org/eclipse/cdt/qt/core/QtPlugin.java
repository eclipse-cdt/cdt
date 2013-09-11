/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.qt.core;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

public class QtPlugin extends Plugin {

    public static final String ID = "org.eclipse.cdt.qt.core";
    public static final String SIGNAL_SLOT_TAGGER_ID = ID + ".signalslot.tagger";

    public static final int SignalSlot_Mask_signal = 1;
    public static final int SignalSlot_Mask_slot = 2;

	private static QtPlugin plugin;
    private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	public QtPlugin() {
		plugin = this;
	}

	public static QtPlugin getDefault() {
		return plugin;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
    public void start(BundleContext bundleContext) throws Exception {
		QtPlugin.context = bundleContext;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
    public void stop(BundleContext bundleContext) throws Exception {
		QtPlugin.context = null;
	}

	public static void log(String e) {
		log(IStatus.INFO, e, null);
	}

	public static void log(Throwable e) {
		String msg= e.getMessage();
		if (msg == null) {
			log("Error", e); //$NON-NLS-1$
		} else {
			log("Error: " + msg, e); //$NON-NLS-1$
		}
	}

	public static void log(String message, Throwable e) {
		Throwable nestedException;
		if (e instanceof CModelException
				&& (nestedException = ((CModelException)e).getException()) != null) {
			e = nestedException;
		}
		log(IStatus.ERROR, message, e);
	}

	public static void log(int code, String msg, Throwable e) {
		getDefault().getLog().log(new Status(code, ID, msg, e));
	}
}
