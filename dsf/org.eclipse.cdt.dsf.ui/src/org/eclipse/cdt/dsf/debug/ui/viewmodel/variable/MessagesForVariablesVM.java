/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.debug.ui.viewmodel.variable;

import org.eclipse.osgi.util.NLS;

/**
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class MessagesForVariablesVM extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.messages"; //$NON-NLS-1$

    public static String VariableColumnPresentation_name;
    public static String VariableColumnPresentation_type;
    public static String VariableColumnPresentation_value;
    public static String VariableColumnPresentation_location;

    public static String VariableVMNode_CannotCastVariable;

	public static String VariableVMNode_Location_column__Error__text_format;
    public static String VariableVMNode_Location_column__text_format;
    public static String VariableVMNode_Description_column__text_format;
    public static String VariableVMNode_Expression_column__text_format;
    public static String VariableVMNode_Name_column__text_format;
    public static String VariableVMNode_NoColumns_column__Error__text_format;
    public static String VariableVMNode_NoColumns_column__text_format;
    public static String VariableVMNode_NoColumns_column__No_string__text_format_with_type;
    public static String VariableVMNode_NoColumns_column__text_format_with_type;
    public static String VariableVMNode_NoColumns_column__No_string__text_format;
    public static String VariableVMNode_Type_column__Error__text_format;
    public static String VariableVMNode_Type_column__text_format;
    public static String VariableVMNode_Value_column__text_format;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, MessagesForVariablesVM.class);
    }

    private MessagesForVariablesVM() {
    }
}
