/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.numberformat.detail;

import org.eclipse.osgi.util.NLS;

/**
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class MessagesForNumberFormatDetailPane extends NLS {
	
    private static final String BUNDLE_NAME = "org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.numberformat.detail.messages"; //$NON-NLS-1$

    public static String NumberFormatDetailPane_Natural_label;
    public static String NumberFormatDetailPane_Decimal_label;
    public static String NumberFormatDetailPane_Hex_label;
    public static String NumberFormatDetailPane_Octal_label;
    public static String NumberFormatDetailPane_Binary_label;
    public static String NumberFormatDetailPane_String_label;
    public static String NumberFormatDetailPane_Other_label;
    public static String NumberFormatDetailPane_Name_label;
    public static String NumberFormatDetailPane_Spaces_label;   
    public static String NumberFormatDetailPane_CarriageReturn_label;
    public static String NumberFormatDetailPane_DotDotDot_label;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, MessagesForNumberFormatDetailPane.class);
    }

    private MessagesForNumberFormatDetailPane() {}
}
