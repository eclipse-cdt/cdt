/**********************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     QNX Software Systems - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.debug.gdbjtag.core;

/**
 * @author Doug Schaefer
 *
 */
public class GDBJtagConstants {
	
	public static final String DEBUGGER_ID = "org.eclipse.cdt.debug.mi.core.CDebuggerNew"; //$NON-NLS-1$
	
	// Debugger
	public static final String ATTR_USE_REMOTE_TARGET = Activator.PLUGIN_ID + ".useRemoteTarget"; //$NON-NLS-1$
	public static final String ATTR_IP_ADDRESS = Activator.PLUGIN_ID + ".ipAddress"; //$NON-NLS-1$
	public static final String ATTR_PORT_NUMBER = Activator.PLUGIN_ID + ".portNumber"; //$NON-NLS-1$
	
	public static final boolean DEFAULT_USE_REMOTE_TARGET = true;
	public static final String DEFAULT_IP_ADDRESS = "localhost"; //$NON-NLS-1$
	public static final int DEFAULT_PORT_NUMBER = 10000;
	
	// Startup
	public static final String ATTR_INIT_COMMANDS = Activator.PLUGIN_ID + ".initCommands"; //$NON-NLS-1$
	public static final String ATTR_LOAD_IMAGE = Activator.PLUGIN_ID + ".loadImage"; //$NON-NLS-1$
	public static final String ATTR_IMAGE_FILE_NAME = Activator.PLUGIN_ID + ".imageFileName"; //$NON-NLS-1$
	public static final String ATTR_USE_DEFAULT_RUN = Activator.PLUGIN_ID + ".useDefaultRun"; //$NON-NLS-1$
	public static final String ATTR_RUN_COMMANDS = Activator.PLUGIN_ID + ".runCommands"; //$NON-NLS-1$

	public static final boolean DEFAULT_LOAD_IMAGE = false;
	public static final boolean DEFAULT_USE_DEFAULT_RUN = true;
	
}
