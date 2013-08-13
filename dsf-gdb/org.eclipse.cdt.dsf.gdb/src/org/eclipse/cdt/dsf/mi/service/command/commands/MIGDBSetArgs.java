/*******************************************************************************
 * Copyright (c) 2008, 2013 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *	   Sergey Prigogin (Google)
 *     Marc Khouzam (Ericsson) - Support empty arguments (bug 412471)
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;

/**
 *      -gdb-set args ARGS
 *
 * Set the inferior program arguments, to be used in the next `-exec-run'.
 * @since 1.1
 */
public class MIGDBSetArgs extends MIGDBSet {

    /** @since 4.0 */
	public MIGDBSetArgs(IMIContainerDMContext dmc) {
        this(dmc, new String[0]);
    }

    /** @since 4.0 */
    public MIGDBSetArgs(IMIContainerDMContext dmc, String[] arguments) {
        super(dmc, null);

    	String[] cmdArray = new String[arguments.length + 1];
    	cmdArray[0] = "args"; //$NON-NLS-1$
    	for (int i = 0; i < arguments.length; i++) {
    		if (arguments[i].isEmpty()) {
    			// An empty parameter can be passed with two single quotes
    			cmdArray[i + 1] = "''"; //$NON-NLS-1$
    		} else {
    			cmdArray[i + 1] = arguments[i];
    		}
    	}
    	setParameters(cmdArray);
    }
}
