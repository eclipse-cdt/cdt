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

package org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.numberformat;

import org.eclipse.osgi.util.NLS;

public class MessagesForNumberFormat extends NLS {
	
    private static final String BUNDLE_NAME = "org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.numberformat.messages"; //$NON-NLS-1$

    public static String NumberFormatContribution_Natural_label;
    public static String NumberFormatContribution_Decimal_label;
    public static String NumberFormatContribution_Hex_label;
    public static String NumberFormatContribution_Octal_label;
    public static String NumberFormatContribution_Binary_label;
    public static String NumberFormatContribution_String_label;

    public static String NumberFormatContribution_EmptyFormatsList_label;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, MessagesForNumberFormat.class);
    }

    private MessagesForNumberFormat() {}
}
