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
 * @since 1.1
 */
public class LaunchVMUpdateMessages extends NLS {
    public static String ThreadsAutomaticUpdatePolicy_name;
    public static String ThreadsManualUpdatePolicy_name;
	
    static {
        // load message values from bundle file
        NLS.initializeMessages(LaunchVMUpdateMessages.class.getName(), LaunchVMUpdateMessages.class);
    }

    private LaunchVMUpdateMessages() {
    }
}
