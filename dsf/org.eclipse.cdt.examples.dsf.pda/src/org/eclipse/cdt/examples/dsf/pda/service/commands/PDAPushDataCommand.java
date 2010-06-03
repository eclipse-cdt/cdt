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
 * Pushes the given value on top of the data stack.
 * 
 * <pre>
 *    C: pushdata {thread_id} {value}
 *    R: ok
 *    
 * Errors:
 *    error: invalid thread
 * </pre>
 */
@Immutable
public class PDAPushDataCommand extends AbstractPDACommand<PDACommandResult> {

    public PDAPushDataCommand(PDAThreadDMContext thread, int value) {
        super(thread, "pushdata " + thread.getID() + " " + value);
    }
    
    @Override
    public PDACommandResult createResult(String resultText) {
        return new PDACommandResult(resultText);
    }
}
