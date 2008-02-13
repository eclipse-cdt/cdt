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
 * Sets a data value in the data stack at the given location
 * 
 * <pre>
 *    C: setdata {index} {value}
 *    R: ok
 * </pre>
 */
public class PDASetDataCommand extends PDACommandBase<PDACommandBaseResult> {

    public PDASetDataCommand(IDMContext context, int index, int value) {
        super(context, "setdata " + index + " " + value);
    }
    
    @Override
    public PDACommandBaseResult createResult(String resultText) {
        return new PDACommandBaseResult(resultText);
    }
}
