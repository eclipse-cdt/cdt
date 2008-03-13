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
import org.eclipse.dd.examples.pda.service.PDAProgramDMContext;

/**
 * Instructs the debugger to exit.
 * 
 * <pre>
 *    C: exit
 *    R: ok
 * </pre>
 */
@Immutable
public class PDAExitCommand extends AbstractPDACommand<PDACommandResult> {

    public PDAExitCommand(PDAProgramDMContext context) {
        super(context, "exit");
    }
    
    @Override
    public PDACommandResult createResult(String resultText) {
        return new PDACommandResult(resultText);
    }
}
