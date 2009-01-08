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

package org.eclipse.cdt.dsf.mi.service.command.commands;


import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIMonitorListProcessesInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * 
 *    monitor list processes
 *    
 *    This command is not available in the current version of GDBServer.  However it should
 *    be available in the future.
 *
 */
public class CLIMonitorListProcesses extends CLICommand<CLIMonitorListProcessesInfo> 
{
    public CLIMonitorListProcesses(IDMContext ctx) {
        super(ctx, "monitor list processes"); //$NON-NLS-1$
    }
    
	@Override
	public CLIMonitorListProcessesInfo getResult(MIOutput output) {
		return (CLIMonitorListProcessesInfo)getMIInfo(output);
	}
	
	public MIInfo getMIInfo(MIOutput out) {
		MIInfo info = null;
		if (out != null) {
			info = new CLIMonitorListProcessesInfo(out);
		}
		return info;
	}
}
