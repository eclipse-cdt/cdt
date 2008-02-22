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

import org.eclipse.dd.examples.pda.service.PDAProgramDMContext;

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
public class PDASetBreakpointCommand extends AbstractPDACommand<PDACommandResult> {

    public PDASetBreakpointCommand(PDAProgramDMContext context, int line) {
        super(context, "set " + (line - 1));
    }
    
    @Override
    public PDACommandResult createResult(String resultText) {
        return new PDACommandResult(resultText);
    }
}
