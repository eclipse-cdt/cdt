/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.transport;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class MemoryTransportPlugin extends AbstractUIPlugin 
{
	private static final String PLUGIN_ID = "org.eclipse.cdt.debug.ui.memory.transport"; //$NON-NLS-1$
	
	private static MemoryTransportPlugin plugin;
	
	public MemoryTransportPlugin() 
	{
		super();
		plugin = this;
	}

	/**
	 * Returns the shared instance.
	 */
	public static MemoryTransportPlugin getDefault() {
		return plugin;
	}
	
	/**
	 * Returns the unique identifier for this plugin.
	 */
	public static String getUniqueIdentifier() {
		return PLUGIN_ID;
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
		}
		else {
			return window.getShell();
		}
		return null;
	}
	
	/**
	 * Get settings section from plugin dialog settings 
	 * @param node - required section. Will create a new one if does not exist
	 * @return dialog settings
	 */
	public IDialogSettings getDialogSettings(String node) {
		IDialogSettings nodeSettings  = getDialogSettings().getSection(node);
		if (nodeSettings == null) {
			nodeSettings = getDialogSettings().addNewSection(node);
		}
		return nodeSettings;
	}
}
