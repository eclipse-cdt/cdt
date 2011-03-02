/*******************************************************************************
 * Copyright (c) 2011 Ericsson 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceRecordDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.CLITraceDumpInfo;


/**
 * GDB tdump CLI command.
 * @since 4.0
 *
 */
public class CLITraceDump extends CLICommand<CLITraceDumpInfo> {
	
	/**
	 * @param ctx trace context
	 */
	public CLITraceDump(ITraceRecordDMContext ctx) {
		super(ctx, "tdump"); //$NON-NLS-1$
	}
	
    @Override
    public CLITraceDumpInfo getResult(MIOutput out) {
        return new CLITraceDumpInfo(out);
    }
} 