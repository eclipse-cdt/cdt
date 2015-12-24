/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.ui.viewmodel.properties;

import org.eclipse.osgi.util.NLS;

/**
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
class MessagesForProperties extends NLS {
    public static String DefaultLabelMessage_label;
    public static String PropertiesUpdateStatus_message;
    
    static {
        // initialize resource bundle
        NLS.initializeMessages(MessagesForProperties.class.getName(), MessagesForProperties.class);
    }

    private MessagesForProperties() {
    }
}
