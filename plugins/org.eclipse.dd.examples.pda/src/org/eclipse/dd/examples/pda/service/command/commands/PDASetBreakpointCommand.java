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
 * Sets a breakpoint at given line
 * 
 * <pre>
 *    C: set {line_number}
 *    R: ok
 *    C: resume
 *    E: resumed client
 *    E: suspended breakpoint line_number
 * </pre>
 */
public class PDASetBreakpointCommand extends PDACommandBase<PDACommandBaseResult> {

    public PDASetBreakpointCommand(IDMContext context, int line) {
        super(context, "set " + line);
    }
    
    @Override
    public PDACommandBaseResult createResult(String resultText) {
        return new PDACommandBaseResult(resultText);
    }
}
