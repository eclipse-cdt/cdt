/*******************************************************************************
 * Copyright (c) 2010 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Freescale Semiconductor - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.memory;

import org.eclipse.osgi.util.NLS;

/**
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class Messages extends NLS {
    static {
        NLS.initializeMessages("org.eclipse.cdt.dsf.gdb.internal.memory.messages", Messages.class); //$NON-NLS-1$
    }

    public static String Err_MemoryServiceNotAvailable;
    public static String Err_MemoryReadFailed;
    public static String Err_MemoryWriteFailed;
    public static String Err_InvalidEncodedAddress;
}
