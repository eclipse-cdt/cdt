/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Monta Vista - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core;


public interface IGDBServerMILaunchConfigurationConstants extends IMILaunchConfigurationConstants {

	/**
	 * Launch configuration attribute key. The "remote target xxx" value.
	 */
	public static final String ATTR_REMOTE_TCP = MIPlugin.getUniqueIdentifier() + ".REMOTE_TCP"; //$NON-NLS-1$
	public static final String ATTR_HOST = MIPlugin.getUniqueIdentifier() + ".HOST"; //$NON-NLS-1$
	public static final String ATTR_PORT = MIPlugin.getUniqueIdentifier() + ".PORT"; //$NON-NLS-1$
	public static final String ATTR_DEV = MIPlugin.getUniqueIdentifier() + ".DEV"; //$NON-NLS-1$
	public static final String ATTR_DEV_SPEED = MIPlugin.getUniqueIdentifier() + ".DEV_SPEED"; //$NON-NLS-1$
}
