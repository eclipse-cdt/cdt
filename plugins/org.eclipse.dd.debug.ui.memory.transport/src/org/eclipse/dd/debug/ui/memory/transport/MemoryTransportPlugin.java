/*******************************************************************************
 * Copyright (c) 2006-2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/

package org.eclipse.dd.debug.ui.memory.transport;

import org.eclipse.ui.plugin.AbstractUIPlugin;

public class MemoryTransportPlugin extends AbstractUIPlugin 
{
	private static final String PLUGIN_ID = "org.eclipse.dd.debug.ui.memory.transport"; //$NON-NLS-1$
	
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
}
