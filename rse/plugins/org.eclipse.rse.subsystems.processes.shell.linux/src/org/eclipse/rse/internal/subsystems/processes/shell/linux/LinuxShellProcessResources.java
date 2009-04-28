/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Martin Oberhuber (Wind River) - initial API and implementation
 * Anna Dushistova  (MontaVista) - [175300][performance] processes.shell.linux subsystem is slow over ssh
 *******************************************************************************/

package org.eclipse.rse.internal.subsystems.processes.shell.linux;

import org.eclipse.osgi.util.NLS;

public class LinuxShellProcessResources extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.rse.internal.subsystems.processes.shell.linux.LinuxShellProcessResources"; //$NON-NLS-1$

	public static String LinuxRemoteProcessService_name;

	public static String LinuxRemoteProcessService_description;

	public static String LinuxRemoteProcessService_monitor_fetchProcesses;
	
	public static String LinuxRemoteProcessService_getSignalTypes_empty;

	public static String LinuxShellProcessService_initHelper;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, LinuxShellProcessResources.class);
	}

	private LinuxShellProcessResources() {
	}
}
