/*******************************************************************************
 * Copyright (c) 2006 PalmSource, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Ewa Matejska (PalmSource) - Adapted from IGDBServerMILaunchConfigurationConstants
 *******************************************************************************/


package org.eclipse.rse.remotecdt;

import org.eclipse.cdt.debug.mi.core.IMILaunchConfigurationConstants;
import org.eclipse.debug.core.DebugPlugin;

public interface IRemoteConnectionConfigurationConstants extends
		IMILaunchConfigurationConstants {
	
	public static final String ATTR_REMOTE_CONNECTION = 
				DebugPlugin.getUniqueIdentifier() + ".REMOTE_TCP";

	/*
	 * ATTR_TCP_PORT: gdbserver port.
	 */  
	public static final String ATTR_TCP_PORT = "2345";
	
	/*
	 * Generic Remote Path and Download options
	 * ATTR_REMOTE_PATH: Path of the binary on the remote.
	 * ATTR_SKIP_DOWNLOAD_TO_TARGET: true if download to remote is not desired.
	 */
	public static final String ATTR_REMOTE_PATH = 
				DebugPlugin.getUniqueIdentifier() + ".ATTR_TARGET_PATH";
	public static final String ATTR_SKIP_DOWNLOAD_TO_TARGET = 
				DebugPlugin.getUniqueIdentifier() + ".ATTR_SKIP_DOWNLOAD_TO_TARGET";

}
