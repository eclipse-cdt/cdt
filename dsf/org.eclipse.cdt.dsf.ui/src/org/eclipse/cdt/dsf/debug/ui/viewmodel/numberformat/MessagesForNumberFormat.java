/*******************************************************************************
 *  Copyright (c) 2006, 2014 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     Wind River Systems, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat;

import org.eclipse.osgi.util.NLS;

/**
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class MessagesForNumberFormat extends NLS {
	public static String FormattedValueVMUtil_Natural_format__label;
	public static String FormattedValueVMUtil_Decimal_format__label;
	public static String FormattedValueVMUtil_Hex_format__label;
	public static String FormattedValueVMUtil_Octal_format__label;
	public static String FormattedValueVMUtil_Binary_format__label;
	public static String FormattedValueVMUtil_String_format__label;
	public static String FormattedValueVMUtil_Other_format__format_text;

	public static String NumberFormatContribution_EmptyFormatsList_label;
	public static String ElementNumberFormatContribution_RestoreToPreference_label;

	public static String FormattedValueLabelText__Value__text_format;
	public static String FormattedValueLabelText__text_format;

	static {
		// initialize resource bundle
		NLS.initializeMessages(MessagesForNumberFormat.class.getName(), MessagesForNumberFormat.class);
	}

	private MessagesForNumberFormat() {
	}
}
