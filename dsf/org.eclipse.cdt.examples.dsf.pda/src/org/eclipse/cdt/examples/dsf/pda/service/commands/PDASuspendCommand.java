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
 * Suspends execution of a single thread.  Can be issued only if the virtual 
 * machine is running.
 * 
 * <pre>
 *    C: suspend {thread_id}
 *    R: ok
 *    E: suspended {thread_id} client
 *    
 * Errors:
 *    error: invalid thread
      error: vm already suspended
 *    error: thread already suspended
 * </pre>
 */
@Immutable
public class PDASuspendCommand extends AbstractPDACommand<PDACommandResult> {

    public PDASuspendCommand(PDAThreadDMContext thread) {
        super(thread, "suspend " + thread.getID());
    }
    
    @Override
    public PDACommandResult createResult(String resultText) {
        return new PDACommandResult(resultText);
    }
}
