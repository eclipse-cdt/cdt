/*******************************************************************************
 * Copyright (c) 2006-2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.traditional;

import org.eclipse.ui.plugin.AbstractUIPlugin;

public class TraditionalRenderingPlugin extends AbstractUIPlugin 
{
	private static final String PLUGIN_ID = "org.eclipse.cdt.debug.ui.memory.traditional"; //$NON-NLS-1$
	
	private static TraditionalRenderingPlugin plugin;
	
	public TraditionalRenderingPlugin() 
	{
		super();
		plugin = this;
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
}
