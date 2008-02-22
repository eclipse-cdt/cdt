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
 * Retrieves data stack information 
 * 
 * <pre>
 *    C: data
 *    R: {value 1}|{value 2}|{value 3}|...|
 * </pre>
 */
public class PDADataCommand extends AbstractPDACommand<PDACommandResult> {

    public PDADataCommand(PDAProgramDMContext context) {
        super(context, "data");
    }
    
    @Override
    public PDACommandResult createResult(String resultText) {
        return new PDACommandResult(resultText);
    }
}
