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
import org.eclipse.cdt.examples.dsf.pda.service.PDAThreadDMContext;

/**
 * Returns from the current frame without executing the rest of instructions.  
 * 
 * <pre>
 * If VM running:
 *    C: drop {thread_id}
 *    R: ok
 *    E: resumed {thread_id} drop
 *    E: suspended {thread_id} drop
 *    
 * If VM suspended:
 *    C: drop {thread_id}
 *    R: ok
 *    E: vmresumed drop
 *    E: vmsuspended {thread_id} drop
 *    
 * Errors:
 *    error: invalid thread
 * </pre>
 */
@Immutable
public class PDADropFrameCommand extends AbstractPDACommand<PDACommandResult> {

    public PDADropFrameCommand(PDAThreadDMContext thread) {
        super(thread, "drop " + thread.getID());
    }
    
    @Override
    public PDACommandResult createResult(String resultText) {
        return new PDACommandResult(resultText);
    }
}
