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

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;

/**
 * -gdb-set dprintf-style STYLE
 * 
 * Set the dprintf output to be handled in one of several different styles enumerated below. 
 * A change of style affects all existing dynamic printfs immediately. 
 * 
 *   gdb
 *     Handle the output using the gdb printf command.
 *   call
 *     Handle the output by calling a function in your program (normally printf).
 *   agent
 *     Have the remote debugging agent (such as gdbserver) handle the output itself. 
 *     This style is only available for agents that support running commands on the target.
 *     
 * @since 4.2
 */
public class MIGDBSetDPrintfStyle extends MIGDBSet {

	public MIGDBSetDPrintfStyle(ICommandControlDMContext dmc, String style) {
		super(dmc, new String[] { "dprintf-style", style }); //$NON-NLS-1$
    }
}
