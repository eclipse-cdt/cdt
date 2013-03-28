/*******************************************************************************
 * Copyright (c) 2013 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIDPrintfInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * This command sets a dynamic printf.
 * @since 4.3
 */
public class CLIDPrintf extends MICommand<CLIDPrintfInfo> {
	public CLIDPrintf(IBreakpointsTargetDMContext ctx, String location, String printfStr) {
        super(ctx, "-interpreter-exec", //$NON-NLS-1$
        		   new String[] {"console", //$NON-NLS-1$
        		                 "dprintf " + location + "," + printfStr}); //$NON-NLS-1$//$NON-NLS-2$
	}
	
	@Override
	public CLIDPrintfInfo getResult(MIOutput output) {
		return new CLIDPrintfInfo(output);
	}

}
