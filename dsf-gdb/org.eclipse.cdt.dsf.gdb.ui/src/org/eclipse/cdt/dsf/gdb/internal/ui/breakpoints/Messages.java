/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.breakpoints;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	public static String ToggleTracepointsTargetFactory_description;
	public static String ToggleTracepointsTargetFactory_name;
	public static String TracepointPropertyPage_integer_negative;
	public static String TracepointPropertyPage_NotAvailable;
	public static String TracepointPropertyPage_FunctionName;
	public static String TracepointPropertyPage_FunctionTracepoint;
	public static String TracepointPropertyPage_Address;
	public static String TracepointPropertyPage_AddressTracepoint;
	public static String TracepointPropertyPage_File;
	public static String TracepointPropertyPage_LineTracepoint;
	public static String TracepointPropertyPage_LineNumber;
	public static String TracepointPropertyPage_Project;
	public static String TracepointPropertyPage_Condition;
	public static String TracepointPropertyPage_InvalidCondition;
	public static String TracepointPropertyPage_IgnoreCount;
	public static String TracepointPropertyPage_PassCount;
	public static String TracepointPropertyPage_Class;
	public static String TracepointPropertyPage_Enabled;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}
}
