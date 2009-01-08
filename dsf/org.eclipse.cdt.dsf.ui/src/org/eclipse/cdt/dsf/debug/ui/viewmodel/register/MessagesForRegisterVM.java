/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.register;

import org.eclipse.osgi.util.NLS;

public class MessagesForRegisterVM extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.cdt.dsf.debug.ui.viewmodel.register.messages"; //$NON-NLS-1$

    public static String RegisterColumnPresentation_description;

    public static String RegisterColumnPresentation_name;

    public static String RegisterColumnPresentation_type;

    public static String RegisterColumnPresentation_value;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, MessagesForRegisterVM.class);
    }

    private MessagesForRegisterVM() {
    }
}
