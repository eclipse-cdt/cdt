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
 * Retrieves command stack information 
 * 
 * <pre>
 *    C: stack
 *    R: {file}|{line}|{function}|{var_1}|{var_2}|...#{file}|{line}|{function}|{var_1}|{var_2}|...#...
 * </pre>
 */
public class PDAStackCommand extends PDACommandBase<PDACommandBaseResult> {

    public PDAStackCommand(IDMContext context) {
        super(context, "stack");
    }
    
    @Override
    public PDACommandBaseResult createResult(String resultText) {
        return new PDACommandBaseResult(resultText);
    }
}
