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
 * Sets a watchpoint on a given variable
 * 
 * <pre>
 *    C: watch {function}::{variable_name} {watch_operation}
 *    R: ok
 *    C: vmresume
 *    R: vmresumed client
 *    E: vmsuspended {thread_id} watch {watch_operation} {function}::{variable_name}
 * </pre>
 */
@Immutable
public class PDAWatchCommand extends AbstractPDACommand<PDACommandResult> {

    public enum WatchOperation { READ, WRITE, BOTH, NONE };
    
    private static int getWatchOperationCode(WatchOperation operation) {
        switch (operation) {
        case READ:
            return 1;
        case WRITE:
            return 2;
        case BOTH:
            return 3;
        default:
            return 0;
        }
    }
    
    public PDAWatchCommand(PDAVirtualMachineDMContext context, String function, String variable, WatchOperation operation) {
        super(context, "watch " + function+ "::" + variable + " " + getWatchOperationCode(operation));
    }
    
    @Override
    public PDACommandResult createResult(String resultText) {
        return new PDACommandResult(resultText);
    }
}
