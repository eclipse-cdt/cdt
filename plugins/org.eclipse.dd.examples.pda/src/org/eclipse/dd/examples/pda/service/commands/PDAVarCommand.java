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
 * Retrieves variable value 
 * 
 * <pre>
 *    C: var {frame_number} {variable_name}
 *    R: {variable_value}
 * </pre>
 */
public class PDAVarCommand extends AbstractPDACommand<PDACommandResult> {

    public PDAVarCommand(PDAProgramDMContext context, int frameId, String name) {
        super(context, "var " + frameId + " " + name);
    }
    
    @Override
    public PDACommandResult createResult(String resultText) {
        return new PDACommandResult(resultText);
    }
}
