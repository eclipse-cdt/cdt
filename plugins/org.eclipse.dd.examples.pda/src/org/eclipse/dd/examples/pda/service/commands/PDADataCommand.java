/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.examples.pda.service.commands;

import org.eclipse.dd.dsf.concurrent.Immutable;
import org.eclipse.dd.examples.pda.service.PDAThreadDMContext;

/**
 * Retrieves data stack information 
 * 
 * <pre>
 *    C: data {thread_id}
 *    R: {value 1}|{value 2}|{value 3}|...|
 *    
 * Errors:
 *    error: invalid thread
 * </pre>
 */
@Immutable
public class PDADataCommand extends AbstractPDACommand<PDADataCommandResult> {

    public PDADataCommand(PDAThreadDMContext thread) {
        super(thread, "data " + thread.getID());
    }
    
    @Override
    public PDADataCommandResult createResult(String resultText) {
        return new PDADataCommandResult(resultText);
    }
}
