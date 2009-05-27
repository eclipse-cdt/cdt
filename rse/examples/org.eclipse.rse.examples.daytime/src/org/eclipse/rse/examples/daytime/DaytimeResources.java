/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Martin Oberhuber (Wind River) - initial API and implementation 
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 *******************************************************************************/

package org.eclipse.rse.examples.daytime;

import org.eclipse.osgi.util.NLS;

/**
 * Resources for externalized Strings of the Daytime subsystem.
 */
public class DaytimeResources extends NLS {
	private static String BUNDLE_NAME = "org.eclipse.rse.examples.daytime.DaytimeResources";//$NON-NLS-1$

	public static String Daytime_Service_Name;
	public static String Daytime_Service_Description;
	public static String Daytime_Connector_Name;
	public static String Daytime_Connector_Description;
	public static String Daytime_Resource_Type;

	public static String DaytimeConnectorService_NotAvailable;

	public static String DaytimeWizard_TestFieldText;
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, DaytimeResources.class);
	}

}
