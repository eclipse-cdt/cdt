/*******************************************************************************
 * Copyright (c) 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;

/**
 * 
 * -gdb-set scheduler-locking [on|step|off]
 * 
 * Set the scheduler locking mode. If it is off, then there is no locking and any thread may run at any time. 
 * If on, then only the current thread may run when the inferior is resumed. The step mode optimizes for 
 * single-stepping; it prevents other threads from preempting the current thread while you are stepping, so 
 * that the focus of debugging does not change unexpectedly. Other threads only rarely (or never) get a chance 
 * to run when you step. They are more likely to run when you `next' over a function call, and they are 
 * completely free to run when you use commands like `continue', `until', or `finish'. However, unless another 
 * thread hits a breakpoint during its timeslice, gdb does not change the current thread away from the thread 
 * that you are debugging. 
 * 
 * @since 4.1
 */
public class MIGDBSetSchedulerLocking extends MIGDBSet 
{
	/**
	 * @param mode The value to be send to GDB.  Can be 'on', 'off', 'step'
	 */
    public MIGDBSetSchedulerLocking(ICommandControlDMContext ctx, String mode) {
        super(ctx, new String[] {"scheduler-locking", mode});//$NON-NLS-1$
    }
}