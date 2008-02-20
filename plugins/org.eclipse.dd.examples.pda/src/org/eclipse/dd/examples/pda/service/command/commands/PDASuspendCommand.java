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

import org.eclipse.dd.examples.pda.service.command.PDACommandControlDMContext;
import org.eclipse.dd.examples.pda.service.command.PDACommandResult;

/**
 * Suspends execution 
 * 
 * <pre>
 *    C: suspend
 *    R: ok
 *    E: suspended client
 * </pre>
 */
public class PDASuspendCommand extends AbstractPDACommand<PDACommandResult> {

    public PDASuspendCommand(PDACommandControlDMContext context) {
        super(context, "suspend");
    }
    
    @Override
    public PDACommandResult createResult(String resultText) {
        return new PDACommandResult(resultText);
    }
}
