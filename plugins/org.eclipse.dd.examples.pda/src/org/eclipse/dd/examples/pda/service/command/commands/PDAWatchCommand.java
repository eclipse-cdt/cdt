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
package org.eclipse.dd.examples.pda.service.command.commands;

import org.eclipse.dd.dsf.datamodel.IDMContext;

/**
 * Sets a watchpoint on a given variable
 * 
 * <pre>
 *    C: watch {function}::{variable_name} {watch_operation}
 *    R: ok
 *    C: resume
 *    R: resume client
 *    E: suspended watch {watch_operation} {function}::{variable_name}
 * </pre>
 */
public class PDAWatchCommand extends PDACommandBase<PDACommandBaseResult> {

    public enum WatchOperation { READ, WRITE, BOTH, NONE };
    
    private static int getWatchOperationCode(WatchOperation operation) {
        switch (operation) {
        case READ:
            return 1;
        case WRITE:
            return 2;
        case BOTH:
            return 3;
        default:
            return 0;
        }
    }
    
    public PDAWatchCommand(IDMContext context, String function, String variable, WatchOperation operation) {
        super(context, "watch " + function+ "::" + variable + " " + getWatchOperationCode(operation));
    }
    
    @Override
    public PDACommandBaseResult createResult(String resultText) {
        return new PDACommandBaseResult(resultText);
    }
}
