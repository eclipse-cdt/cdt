/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.pda.service.commands;

import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.examples.dsf.pda.service.PDAVirtualMachineDMContext;

/**
 * Sets a breakpoint at given line
 * 
 * <pre>
 * Suspend a single thread:
 *    C: set {line_number} 0
 *    R: ok
 *    C: resume {thread_id}
 *    E: resumed {thread_id} client
 *    E: suspended {thread_id} breakpoint line_number
 *    
 * Suspend the VM:
 *    C: set {line_number} 1
 *    R: ok
 *    C: vmresume
 *    E: vmresumed client
 *    E: vmsuspended {thread_id} breakpoint line_number
 * </pre>
 */
@Immutable
public class PDASetBreakpointCommand extends AbstractPDACommand<PDACommandResult> {

    public PDASetBreakpointCommand(PDAVirtualMachineDMContext context, int line, boolean stopVM) {
        super(context, 
              "set " + 
              (line - 1) + " " + 
              (stopVM ? "1" : "0"));
    }
    
    @Override
    public PDACommandResult createResult(String resultText) {
        return new PDACommandResult(resultText);
    }
}
