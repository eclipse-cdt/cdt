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
 * Sets what events cause the execution to stop.
 * 
 * <pre>
 *    C: eval {thread_id} {instruction}%20{parameter}|{instruction}%20{parameter}|...
 *    R: ok
 *    E: resumed {thread_id} client
 *    E: evalresult result
 *    E: suspended {thread_id} eval
 *    
 * Errors:
 *    error: invalid thread
 *    error: cannot evaluate while vm is suspended
 *    error: thread running        
 * </pre>
 * 
 * Where event_name could be <code>unimpinstr</code> or <code>nosuchlabel</code>.  
 */
@Immutable
public class PDAEvalCommand extends AbstractPDACommand<PDACommandResult> {

    public PDAEvalCommand(PDAThreadDMContext thread, String operation) {
        super(thread, "eval " + thread.getID() + " " + operation);
    }
    
    @Override
    public PDACommandResult createResult(String resultText) {
        return new PDACommandResult(resultText);
    }
}
