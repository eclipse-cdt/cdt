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
 * Sets a data value in the data stack at the given location
 * 
 * <pre>
 *    C: setdata {thread_id} {index} {value}
 *    R: ok
 *    
 * Errors:
 *    error: invalid thread
 * </pre>
 */
@Immutable
public class PDASetDataCommand extends AbstractPDACommand<PDACommandResult> {

    public PDASetDataCommand(PDAThreadDMContext thread, int index, String value) {
        super(thread, "setdata " + thread.getID() + " " + index + " " + value);
    }
    
    @Override
    public PDACommandResult createResult(String resultText) {
        return new PDACommandResult(resultText);
    }
}
