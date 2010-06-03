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
 * Pops the top value from the data stack  
 * 
 * <pre>
 *    C: popdata {thread_id}
 *    R: ok
 *    
 * Errors:
 *    error: invalid thread
 * </pre>
 */
@Immutable
public class PDAPopDataCommand extends AbstractPDACommand<PDACommandResult> {

    public PDAPopDataCommand(PDAThreadDMContext thread) {
        super(thread, "popdata " + thread.getID());
    }
    
    @Override
    public PDACommandResult createResult(String resultText) {
        return new PDACommandResult(resultText);
    }
}
