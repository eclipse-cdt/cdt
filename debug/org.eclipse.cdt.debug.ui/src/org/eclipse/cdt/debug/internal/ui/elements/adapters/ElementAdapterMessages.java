/*******************************************************************************
 * Copyright (c) 2010 CodeSourcery and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * CodeSourcery - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.elements.adapters;

import org.eclipse.osgi.util.NLS;

public class ElementAdapterMessages extends NLS {
    public static String RegistersViewColumnPresentation_0;
    public static String RegistersViewColumnPresentation_1;
    public static String RegistersViewColumnPresentation_2;

    static {
        // initialize resource bundle
        NLS.initializeMessages(ElementAdapterMessages.class.getName(), ElementAdapterMessages.class);
    }

    private ElementAdapterMessages() {
    }
}
