/*******************************************************************************
 * Copyright (c) 2006, 2014 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Abeer Bagul (Tensilica) - Updated error message (Bug 339048)
 *     Jason Litton (Sage Electronic Engineering, LLC) - Added support for dynamic tracing option (Bug 379169)
 *     Alvaro Sanchez-Leon (Ericsson AB) - Each memory context needs a different MemoryRetrieval (Bug 250323)
 *******************************************************************************/
package org.eclipse.cdt.llvm.dsf.lldb.core.internal;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class LLDBCorePlugin extends Plugin {

	/**
	 * LLDB Core Plug-in ID
	 */
	public static final String PLUGIN_ID = "org.eclipse.cdt.llvm.dsf.lldb.core"; //$NON-NLS-1$
	private static LLDBCorePlugin plugin;

	@Override
    public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
    public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static LLDBCorePlugin getDefault() {
		return plugin;
	}
}
