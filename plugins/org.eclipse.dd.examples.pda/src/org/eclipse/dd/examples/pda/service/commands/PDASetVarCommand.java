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
 * Sets a variable value 
 * 
 * <pre>
 *    C: setvar {frame_number} {variable} {value}
 *    R: ok
 * </pre>
 */
public class PDASetVarCommand extends AbstractPDACommand<PDACommandResult> {

    public PDASetVarCommand(PDAProgramDMContext context, int frame, String variable, String value) {
        super(context, "setvar " + frame + " " + variable + " " + value);
    }
    
    @Override
    public PDACommandResult createResult(String resultText) {
        return new PDACommandResult(resultText);
    }
}
