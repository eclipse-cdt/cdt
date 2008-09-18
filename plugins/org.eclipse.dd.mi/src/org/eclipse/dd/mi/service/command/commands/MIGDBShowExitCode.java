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
 *******************************************************************************/

package org.eclipse.dd.mi.service.command.commands;

import org.eclipse.dd.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.dd.mi.service.command.MIControlDMContext;
import org.eclipse.dd.mi.service.command.output.MIGDBShowExitCodeInfo;
import org.eclipse.dd.mi.service.command.output.MIOutput;

/**
 * 
 *-data-evaluate-expression $_exitcode
 * ^done,value="10"
 *
 *   Show the current value of a $_exitcode
 * 
 */
public class MIGDBShowExitCode extends MIDataEvaluateExpression<MIGDBShowExitCodeInfo> {

    /**
     * @since 1.1
     */
    public MIGDBShowExitCode(ICommandControlDMContext ctx) {
        super(ctx, "$_exitcode"); //$NON-NLS-1$
    }
    
    @Deprecated
    public MIGDBShowExitCode(MIControlDMContext ctx) {
        this ((ICommandControlDMContext)ctx);
    }
    
    @Override
    public MIGDBShowExitCodeInfo getResult(MIOutput output) {
        return new MIGDBShowExitCodeInfo(output);
    }
}
