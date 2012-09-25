/*******************************************************************************
 * Copyright (c) 2012 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIInfoBreakInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * 'info break [BP_REFERENCE] will return information about
 * the specified breakpoint.  We use it to find out to which
 * inferiors a breakpoint is applicable.
 * @since 4.2
 */
public class CLIInfoBreak extends CLICommand<CLIInfoBreakInfo> {

	public CLIInfoBreak(IDMContext ctx, int bpReference) {
        super(ctx, "info break " + Integer.toString(bpReference)); //$NON-NLS-1$
	};

    @Override
	public CLIInfoBreakInfo getResult(MIOutput MIresult) {
		return new CLIInfoBreakInfo(MIresult);
	}
}
