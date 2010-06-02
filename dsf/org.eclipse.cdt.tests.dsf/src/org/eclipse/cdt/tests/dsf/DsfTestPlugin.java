/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class DsfTestPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.tests.dsf"; //$NON-NLS-1$

	// The shared instance
	private static DsfTestPlugin fgPlugin;
    private static BundleContext fgBundleContext; 
	
	/**
	 * The constructor
	 */
	public DsfTestPlugin() {
		fgPlugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	@Override
    public void start(BundleContext context) throws Exception {
        fgBundleContext = context;
		super.start(context);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
    public void stop(BundleContext context) throws Exception {
		super.stop(context);
        fgBundleContext = null;
        fgPlugin = null;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static DsfTestPlugin getDefault() {
		return fgPlugin;
	}

    public static BundleContext getBundleContext() {
        return fgBundleContext;
    }

    public static void failRequest(RequestMonitor rm, int code, String message) {
        rm.setStatus(new Status(IStatus.ERROR, PLUGIN_ID, code, message, null));
        rm.done();
    }
    

}
