/*******************************************************************************
 * Copyright (c) 2008, 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;

/**
 * 
 *      -gdb-set args ARGS
 *
 * Set the inferior program arguments, to be used in the next `-exec-run'.
 * @since 1.1
 * 
 */
public class MIGDBSetArgs extends MIGDBSet 
{
    public MIGDBSetArgs(ICommandControlDMContext dmc) {
        super(dmc, new String[] {"args"}); //$NON-NLS-1$
    }
    
    public MIGDBSetArgs(ICommandControlDMContext dmc, String arguments) {
        super(dmc, null);

    	// We do not want to quote the arguments of this command so we must
    	// split them into individual strings
    	String[] argArray = arguments.split(" "); //$NON-NLS-1$
    	String[] cmdArray = new String[argArray.length + 1];
    	cmdArray[0] = "args"; //$NON-NLS-1$
    	for (int i=0; i<argArray.length; i++) {
    		cmdArray[i+1] = argArray[i];
    	}
    	setParameters(cmdArray);
    }
}