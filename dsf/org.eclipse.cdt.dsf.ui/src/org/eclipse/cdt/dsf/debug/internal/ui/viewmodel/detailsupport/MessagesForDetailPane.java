/*******************************************************************************
 *  Copyright (c) 2006, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River Systems, Inc. - extended implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.detailsupport;

import org.eclipse.osgi.util.NLS;

public class MessagesForDetailPane extends NLS {
    public static String NumberFormatDetailPane_Name;
    public static String NumberFormatDetailPane_Description;
	public static String DetailPane_Copy;
	public static String DetailPane_LabelPattern;
	public static String DetailPane_Select_All;
    public static String PaneWordWrapAction_WrapText;
    public static String PaneMaxLengthAction_MaxLength;
    public static String PaneMaxLengthDialog_ConfigureDetails;
	public static String PaneMaxLengthDialog_MaxCharactersToDisplay;
	public static String PaneMaxLengthDialog_IntegerCannotBeNegative;
	public static String PaneMaxLengthDialog_EnterAnInteger;
    
    static {
        // initialize resource bundle
        NLS.initializeMessages(MessagesForDetailPane.class.getName(), MessagesForDetailPane.class);
    }

    private MessagesForDetailPane() {
    }
}
