/*******************************************************************************
 * Copyright (c) 2016 Ericsson.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.llvm.dsf.lldb.ui.internal;

import org.eclipse.cdt.llvm.dsf.lldb.core.internal.LLDBCorePlugin;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class LLDBUIPlugin extends AbstractUIPlugin {

	/**
	 * LLDB UI Plug-in ID
	 */
	public static final String PLUGIN_ID = "org.eclipse.cdt.llvm.dsf.lldb.ui"; //$NON-NLS-1$
	private static LLDBUIPlugin plugin;
	private static IPreferenceStore fCorePreferenceStore;
	private static IPreferenceStore fPreferenceStore;

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
	public static LLDBUIPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the preference store for this plug-in.
	 */
	@Override
	public IPreferenceStore getPreferenceStore() {
		if (fPreferenceStore == null) {
			fPreferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, PLUGIN_ID);
		}
		return fPreferenceStore;
	}

	/**
	 * Returns the preference store for the Core plug-in.
	 *
	 * @return the Core plug-in preference store
	 */
	public IPreferenceStore getCorePreferenceStore() {
		if (fCorePreferenceStore == null) {
			fCorePreferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, LLDBCorePlugin.PLUGIN_ID);
		}
		return fCorePreferenceStore;
	}

}
