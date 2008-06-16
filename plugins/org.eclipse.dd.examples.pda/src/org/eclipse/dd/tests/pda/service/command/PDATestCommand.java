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
package org.eclipse.dd.tests.pda.service.command;

import org.eclipse.dd.examples.pda.service.PDAVirtualMachineDMContext;
import org.eclipse.dd.examples.pda.service.commands.AbstractPDACommand;
import org.eclipse.dd.examples.pda.service.commands.PDACommandResult;

/**
 * 
 */
class PDATestCommand extends AbstractPDACommand<PDACommandResult> {
    PDATestCommand(PDAVirtualMachineDMContext context, String command) {
        super(context, command);
    }
    
    @Override
    public PDACommandResult createResult(String resultText) {
        return new PDACommandResult(resultText);
    }
}
