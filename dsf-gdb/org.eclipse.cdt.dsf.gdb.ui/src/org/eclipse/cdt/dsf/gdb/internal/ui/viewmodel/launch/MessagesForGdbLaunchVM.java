/*******************************************************************************
 * Copyright (c) 2008, 2015 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Marc Khouzam (Ericsson) - Support for exited processes in the debug view (bug 407340)
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.launch;

import org.eclipse.osgi.util.NLS;

/**
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class MessagesForGdbLaunchVM extends NLS {
    public static String ThreadVMNode_No_columns__text_format;
    public static String ThreadVMNode_No_columns__Error__label;
    public static String ContainerVMNode_No_columns__text_format;
    public static String ContainerVMNode_No_columns__exited_format;
    public static String ContainerVMNode_No_columns__Error__label;
    /** since 2.3 */
    public static String ContainerVMNode_filtered_running_threads;
    public static String GroupVMNode_No_columns__text_format;
    
    static {
        // initialize resource bundle
        NLS.initializeMessages(MessagesForGdbLaunchVM.class.getName(), MessagesForGdbLaunchVM.class);
    }

    private MessagesForGdbLaunchVM() {
    }
}
