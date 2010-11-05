/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
    static {
        // initialize resource bundle
        NLS.initializeMessages(/*Messages.class.getName()*/"org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.Message", Messages.class); //$NON-NLS-1$
    }

    private Messages() {}

    public static String Internal_Error;
    public static String More_Children;
}
