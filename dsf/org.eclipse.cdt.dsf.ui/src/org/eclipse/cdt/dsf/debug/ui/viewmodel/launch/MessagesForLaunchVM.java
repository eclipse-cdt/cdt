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

package org.eclipse.cdt.dsf.debug.ui.viewmodel.launch;

import org.eclipse.osgi.util.NLS;

/**
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class MessagesForLaunchVM extends NLS {
    public static String StackFramesVMNode_No_columns__Incomplete_stack_marker__text_format;
    public static String StackFramesVMNode_No_columns__text_format;
    public static String StackFramesVMNode_No_columns__No_line__text_format;
    public static String StackFramesVMNode_No_columns__No_function__text_format;
    public static String StackFramesVMNode_No_columns__No_module__text_format;
    public static String StackFramesVMNode_No_columns__Address_only__text_format;
    
    public static String AbstractContainerVMNode_No_columns__text_format;
    public static String AbstractContainerVMNode_No_columns__Error__label;

    public static String AbstractThreadVMNode_No_columns__text_format;
    public static String AbstractThreadVMNode_No_columns__Error__label;
    
    public static String State_change_reason__Unknown__label;
    public static String State_change_reason__User_request__label;
    public static String State_change_reason__Step__label;
    public static String State_change_reason__Breakpoint__label;
    public static String State_change_reason__Exception__label;
    public static String State_change_reason__Container__label;
    public static String State_change_reason__Watchpoint__label;
    public static String State_change_reason__Signal__label;
    public static String State_change_reason__Shared_lib__label;
    public static String State_change_reason__Error__label;
    public static String State_change_reason__Evaluation__label;
    public static String State_change_reason__EventBreakpoint__label;

    static {
        // initialize resource bundle
        NLS.initializeMessages(MessagesForLaunchVM.class.getName(), MessagesForLaunchVM.class);
    }

    private MessagesForLaunchVM() {
    }
}
