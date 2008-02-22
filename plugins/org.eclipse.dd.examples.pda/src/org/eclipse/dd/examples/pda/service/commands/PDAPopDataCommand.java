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
 * Pops the top value from the data stack  
 * 
 * <pre>
 *    C: popdata
 *    R: ok
 * </pre>
 */
public class PDAPopDataCommand extends AbstractPDACommand<PDACommandResult> {

    public PDAPopDataCommand(PDAProgramDMContext context) {
        super(context, "popdata");
    }
    
    @Override
    public PDACommandResult createResult(String resultText) {
        return new PDACommandResult(resultText);
    }
}
