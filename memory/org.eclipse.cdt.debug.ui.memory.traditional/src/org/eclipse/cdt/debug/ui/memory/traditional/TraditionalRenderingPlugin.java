/*******************************************************************************
 * Copyright (c) 2006-2015 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.traditional;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class TraditionalRenderingPlugin extends AbstractUIPlugin {
	private static final String PLUGIN_ID = "org.eclipse.cdt.debug.ui.memory.traditional"; //$NON-NLS-1$

	private static TraditionalRenderingPlugin plugin;

	public TraditionalRenderingPlugin() {
		super();
		plugin = this;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		new TraceOptions(context, PLUGIN_ID);
	}

	/**
	 * Returns the shared instance.
	 */
	public static TraditionalRenderingPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the unique identifier for this plugin.
	 */
	public static String getUniqueIdentifier() {
		return PLUGIN_ID;
	}

	/**
	 * Returns the workbench's display.
	 */
	public static Display getStandardDisplay() {
		return PlatformUI.getWorkbench().getDisplay();
	}

	/**
	 * Returns the currently active workbench window shell or <code>null</code>
	 * if none.
	 *
	 * @return the currently active workbench window shell or <code>null</code>
	 */
	public static Shell getShell() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
			if (windows.length > 0) {
				return windows[0].getShell();
			}
		} else {
			return window.getShell();
		}
		return null;
	}
}
