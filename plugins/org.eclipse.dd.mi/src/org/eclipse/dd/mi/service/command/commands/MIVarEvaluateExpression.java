/*******************************************************************************
 * Copyright (c) 2000, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *     Ericsson				- Modified for handling of frame contexts
 *******************************************************************************/

package org.eclipse.dd.mi.service.command.commands;

import org.eclipse.dd.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.dd.mi.service.command.MIControlDMContext;
import org.eclipse.dd.mi.service.command.output.MIOutput;
import org.eclipse.dd.mi.service.command.output.MIVarEvaluateExpressionInfo;

/**
 * 
 *     -var-evaluate-expression NAME
 *
 *  Evaluates the expression that is represented by the specified
 * variable object and returns its value as a string in the current format
 * specified for the object:
 *
 *      value=VALUE
 * 
 */
public class MIVarEvaluateExpression extends MICommand<MIVarEvaluateExpressionInfo> {
    
    /**
     * @since 1.1
     */
    public MIVarEvaluateExpression(ICommandControlDMContext dmc, String name) {
        super(dmc, "-var-evaluate-expression", new String[] { name }); //$NON-NLS-1$
    }

    public MIVarEvaluateExpression(MIControlDMContext dmc, String name) {
        this ((ICommandControlDMContext)dmc, name);
    }
    
    @Override
    public MIVarEvaluateExpressionInfo getResult(MIOutput out) {
        return new MIVarEvaluateExpressionInfo(out);
    }
}
