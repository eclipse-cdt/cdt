/*******************************************************************************
 * Copyright (c) 2000, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Ericsson				- Modified for handling of contexts
 *******************************************************************************/

package org.eclipse.dd.mi.service.command.commands;

import org.eclipse.dd.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.dd.mi.service.command.output.MIInfo;


/**	
 *   -file-exec-file [FILE]
 *   
 *   Specify the executable file to be debugged. Unlike `-file-exec-and-symbols', 
 *   the symbol table is not read from this file. If used without argument, GDB 
 *   clears the information about the executable file. No output is produced, 
 *   except a completion notification.
 */
public class MIFileExecFile extends MICommand<MIInfo>
{
    public MIFileExecFile(ICommandControlDMContext dmc, String file) {
        super(dmc, "-file-exec-file", null, new String[] {file}); //$NON-NLS-1$
    }
    
    public MIFileExecFile(ICommandControlDMContext dmc) {
        super(dmc, "-file-exec-file"); //$NON-NLS-1$
    }
}
