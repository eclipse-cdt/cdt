/*******************************************************************************
 *  Copyright (c) 2006, 2015 IBM Corporation and others.
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

package org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.numberformat.detail;

import org.eclipse.osgi.util.NLS;

/**
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class MessagesForNumberFormatDetailPane extends NLS {
	public static String NumberFormatDetailPane_format_separator__label;
	public static String NumberFormatDetailPane_Name_label;
	public static String NumberFormatDetailPane_Spaces_label;
	public static String NumberFormatDetailPane_CarriageReturn_label;
	public static String NumberFormatDetailPane_DotDotDot_label;
	public static String NumberFormatDetailPane__End_parentheses;

	static {
		// initialize resource bundle
		NLS.initializeMessages(MessagesForNumberFormatDetailPane.class.getName(),
				MessagesForNumberFormatDetailPane.class);
	}

	private MessagesForNumberFormatDetailPane() {
	}
}
