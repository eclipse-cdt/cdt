/*******************************************************************************
  * Copyright (c) 2009, 2013 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Marc Khouzam (Ericsson) - Support for dynamic printf (bug 400628)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.breakpoints;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	public static String ToggleTracepointsTargetFactory_description;
	public static String ToggleTracepointsTargetFactory_name;
	public static String PropertyPage_integer_negative;
	public static String PropertyPage_NotAvailable;
	public static String PropertyPage_FunctionName;
	public static String TracepointPropertyPage_FunctionTracepoint;
	public static String PropertyPage_Address;
	public static String TracepointPropertyPage_AddressTracepoint;
	public static String PropertyPage_File;
	public static String TracepointPropertyPage_LineTracepoint;
	public static String PropertyPage_LineNumber;
	public static String PropertyPage_Project;
	public static String PropertyPage_Condition;
	public static String PropertyPage_InvalidCondition;
	public static String PropertyPage_IgnoreCount;
	public static String TracepointPropertyPage_PassCount;
	public static String PropertyPage_Class;
	public static String PropertyPage_Enabled;

	public static String GdbThreadFilterEditor_Thread;
	public static String GdbThreadFilterEditor_RestrictToSelected;
	
	public static String ToggleDynamicPrintfTargetFactory_description;
	public static String ToggleDynamicPrintfTargetFactory_name;
	public static String DynamicPrintfPropertyPage_FunctionDynamicPrintf;
	public static String DynamicPrintfPropertyPage_AddressDynamicPrintf;
	public static String DynamicPrintfPropertyPage_LineDynamicPrintf;
	public static String DynamicPrintfPropertyPage_Message;
	public static String DynamicPrintfPropertyPage_InvalidMessage;

	static {
		// initialize resource bundle
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}
}
