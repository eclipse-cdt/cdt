/*******************************************************************************
 * Copyright (c) 2008  Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/

package org.eclipse.dd.mi.service.command.commands;

import org.eclipse.dd.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.dd.mi.service.command.output.MIInfo;


/**	
 *   -file-exec-and-symbols [FILE]
 *   
 *   Specify the executable file to be debugged. Unlike `-file-exec-and-symbols', 
 *   the symbol table is not read from this file. If used without argument, GDB 
 *   clears the information about the executable file. No output is produced, 
 *   except a completion notification.
 */
public class MIFileExecAndSymbols extends MICommand<MIInfo>
{
    public MIFileExecAndSymbols(ICommandControlDMContext dmc, String file) {
        super(dmc, "-file-exec-and-symbols", null, new String[] {file}); //$NON-NLS-1$
    }
    
    public MIFileExecAndSymbols(ICommandControlDMContext dmc) {
        super(dmc, "-file-exec-and-symbols"); //$NON-NLS-1$
    }
}
