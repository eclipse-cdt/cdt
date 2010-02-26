/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIThreadInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * 
 *    thread
 *    
 *    [Current thread is 1 (Thread 0xb7cc56b0 (LWP 5488))]
 *    
 * @since 3.0
 *
 */
public class CLIThread extends CLICommand<CLIThreadInfo> {

    public CLIThread(IContainerDMContext ctx) {
		super(ctx, "thread"); //$NON-NLS-1$
	}

	@Override
	public CLIThreadInfo getResult(MIOutput output) {
		return new CLIThreadInfo(output);
	}
}
