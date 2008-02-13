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
 * Pushes the given value on top of the data stack.
 * 
 * <pre>
 *    C: pushdata {value}
 *    R: ok
 * </pre>
 */
public class PDAPushDataCommand extends PDACommandBase<PDACommandBaseResult> {

    public PDAPushDataCommand(IDMContext context, int value) {
        super(context, "pushdata " + value);
    }
    
    @Override
    public PDACommandBaseResult createResult(String resultText) {
        return new PDACommandBaseResult(resultText);
    }
}
