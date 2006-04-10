/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.ui;

import org.eclipse.osgi.util.NLS;

public class SystemPropertyResources extends NLS
{
	private static String BUNDLE_NAME = "org.eclipse.rse.ui.SystemPropertyResources";//$NON-NLS-1$

//	 ------------------------------
	// PROPERTY SHEET VALUES
	// ------------------------------
	// PROPERTY SHEET VALUES: GENERIC

	public static String RESID_PROPERTY_NAME_LABEL;
	public static String RESID_PROPERTY_NAME_TOOLTIP;

	public static String RESID_PROPERTY_TYPE_LABEL; 
	public static String RESID_PROPERTY_TYPE_TOOLTIP;

	public static String RESID_PROPERTY_DESCRIPTION_LABEL; 
	public static String RESID_PROPERTY_DESCRIPTION_TOOLTIP; 

	public static String RESID_PROPERTY_FILTERTYPE_VALUE;

	public static String RESID_TERM_NOTAPPLICABLE;
	public static String RESID_TERM_NOTAVAILABLE;

	public static String RESID_PORT_DYNAMICSELECT;
	public static String RESID_PROPERTY_INHERITED;
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, SystemPropertyResources.class);
	}
}