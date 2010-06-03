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
 * Retrieves data stack information 
 * 
 * <pre>
 *    C: children {thread_id} {frame_id} {variable_name}
 *    R: {child variable 1}|{child variable 2}|{child variable 3}|...|
 *    
 * Errors:
 *    error: invalid thread
 * </pre>
 */
@Immutable
public class PDAChildrenCommand extends AbstractPDACommand<PDAListResult> {

    public PDAChildrenCommand(PDAThreadDMContext thread, int frameId, String name  ) {
        super(thread, "children " + thread.getID() + " " + frameId + " " + name);
    }
    
    @Override
    public PDAListResult createResult(String resultText) {
        return new PDAListResult(resultText);
    }
}
