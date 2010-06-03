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
 * Executes instructions until the current subroutine is finished 
 * 
 * <pre>
 * If VM running:
 *    C: stepreturn {thread_id}
 *    R: ok
 *    E: resumed {thread_id} client
 *    E: suspended {thread_id} step
 *    
 * If VM suspended:
 *    C: stepreturn {thread_id}
 *    R: ok
 *    E: vmresumed client
 *    E: vmsuspended {thread_id} step
 *    
 * Errors:
 *    error: invalid thread
 * </pre>
 */
@Immutable
public class PDAStepReturnCommand extends AbstractPDACommand<PDACommandResult> {

    public PDAStepReturnCommand(PDAThreadDMContext thread) {
        super(thread, "stepreturn " + thread.getID());
    }
    
    @Override
    public PDACommandResult createResult(String resultText) {
        return new PDACommandResult(resultText);
    }
}
