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
 * Retrieves command stack frame information 
 * 
 * <pre>
 *    C: stack {thread_id} {frame_number}
 *    R: {file}|{line}|{function}|{var_1}|{var_2}|...
 *    
 * Errors:
 *    error: invalid thread
 * </pre>
 */
@Immutable
public class PDAFrameCommand extends AbstractPDACommand<PDAFrameCommandResult> {

    public PDAFrameCommand(PDAThreadDMContext thread, int frameNum) {
        super(thread, "frame " + thread.getID() + " " + frameNum);
    }
    
    @Override
    public PDAFrameCommandResult createResult(String resultText) {
        return new PDAFrameCommandResult(resultText);
    }
}
