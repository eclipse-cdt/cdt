/*******************************************************************************
 * Copyright (c) 2019 Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - initial API and implementatin
 *******************************************************************************/
package org.eclipse.cdt.lsp.internal.ui;

import org.eclipse.cdt.lsp.core.Activator;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

//FIXME: try to remove this class during upcoming preference access rework
public class LspUiActivator implements BundleActivator {

	private static LspUiActivator plugin;
	private IPreferenceStore preferenceStore;

	public static LspUiActivator getDefault() {
		return plugin;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		preferenceStore = null;
		plugin = null;
	}

	//FIXME: currently we are still using "core" bundle to store preferences, to be revisited
	public IPreferenceStore getLspCorePreferences() {
		if (preferenceStore == null) {
			preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, Activator.PLUGIN_ID);
		}
		return preferenceStore;
	}

}
