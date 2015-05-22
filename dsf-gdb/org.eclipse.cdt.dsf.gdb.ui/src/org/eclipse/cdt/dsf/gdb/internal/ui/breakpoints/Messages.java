/*******************************************************************************
  * Copyright (c) 2009, 2015 Wind River Systems, Inc. and others.
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
	public static String ToggleDynamicPrintfTargetFactory_description;
	public static String ToggleDynamicPrintfTargetFactory_name;
	public static String Default_AddressDynamicPrintf_String;
	public static String Default_LineDynamicPrintf_String;

	public static String PropertyPage_integer_negative;
	public static String PropertyPage_NotAvailable;
	public static String PropertyPage_FunctionName;
	public static String PropertyPage_Address;
	public static String PropertyPage_File;
	public static String PropertyPage_LineNumber;
	public static String PropertyPage_Project;
	public static String PropertyPage_Condition;
	public static String PropertyPage_InvalidCondition;
	public static String PropertyPage_IgnoreCount;
	public static String TracepointPropertyPage_PassCount;
	public static String PropertyPage_function_value_errorMessage;
	public static String PropertyPage_Class;
	public static String PropertyPage_Enabled;
	public static String PropertyPage_lineNumber_errorMessage;
	public static String DynamicPrintfPropertyPage_PrintString;

	public static String GdbThreadFilterEditor_Thread;
	public static String GdbThreadFilterEditor_RestrictToSelected;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}
}
