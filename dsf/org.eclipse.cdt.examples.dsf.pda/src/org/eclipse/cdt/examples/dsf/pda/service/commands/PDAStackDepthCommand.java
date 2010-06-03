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
 * Retrieves command stack depth 
 * 
 * <pre>
 *    C: stackdepth {thread_id}
 *    R: {depth}
 *    
 * Errors:
 *    error: invalid thread
 * </pre>
 */
@Immutable
public class PDAStackDepthCommand extends AbstractPDACommand<PDAStackDepthCommandResult> {

    public PDAStackDepthCommand(PDAThreadDMContext thread) {
        super(thread, "stackdepth " + thread.getID());
    }
    
    @Override
    public PDAStackDepthCommandResult createResult(String resultText) {
        return new PDAStackDepthCommandResult(resultText);
    }
}
