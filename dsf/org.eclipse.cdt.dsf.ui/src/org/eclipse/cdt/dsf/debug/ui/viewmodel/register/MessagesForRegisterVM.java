/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.register;

import org.eclipse.osgi.util.NLS;

/**
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class MessagesForRegisterVM extends NLS {
	public static String RegisterColumnPresentation_description;
	public static String RegisterColumnPresentation_name;
	public static String RegisterColumnPresentation_type;
	public static String RegisterColumnPresentation_value;

	public static String RegisterGroupVMNode_Name_column__text_format;
	public static String RegisterGroupVMNode_Description_column__text_format;
	public static String RegisterGroupVMNode_Expression_column__text_format;
	public static String RegisterGroupVMNode_No_columns__text_format;
	public static String RegisterGroupVMNode_No_columns__Error__text_format;

	public static String RegisterVMNode_Description_column__text_format;
	public static String RegisterVMNode_Name_column__text_format;
	public static String RegisterVMNode_Expression_column__text_format;
	public static String RegisterVMNode_Type_column__text_format;
	public static String RegisterVMNode_No_columns__text_format;
	public static String RegisterVMNode_No_columns__text_format_with_type;
	public static String RegisterVMNode_No_columns__Error__text_format;

	public static String RegisterBitFieldVMNode_Name_column__text_format;
	public static String RegisterBitFieldVMNode_Description_column__text_format;
	public static String RegisterBitFieldVMNode_Type_column__text_format;
	public static String RegisterBitFieldVMNode_Value_column__With_mnemonic__text_format;
	public static String RegisterBitFieldVMNode_Expression_column__text_format;
	public static String RegisterBitFieldVMNode_No_columns__text_format;
	public static String RegisterBitFieldVMNode_No_columns__text_format_with_type;
	public static String RegisterBitFieldVMNode_No_columns__With_mnemonic__text_format;
	public static String RegisterBitFieldVMNode_No_columns__With_mnemonic__text_format_with_type;
	public static String RegisterBitFieldVMNode_No_columns__Error__text_format;

	static {
		// initialize resource bundle
		NLS.initializeMessages(MessagesForRegisterVM.class.getName(), MessagesForRegisterVM.class);
	}

	private MessagesForRegisterVM() {
	}
}
