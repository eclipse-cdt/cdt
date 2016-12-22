/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.gdb.ui.console;

import org.eclipse.osgi.util.NLS;

/**
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class GdbExtendedConsoleMessages extends NLS {
    public static String Request_Thread_Info;
    public static String Request_Thread_Info_Tip;
    public static String Set_Special_Background;
    public static String Set_Special_Background_Tip;
    
    static {
        // initialize resource bundle
        NLS.initializeMessages(GdbExtendedConsoleMessages.class.getName(), GdbExtendedConsoleMessages.class);
    }

    private GdbExtendedConsoleMessages() {
    }
}
