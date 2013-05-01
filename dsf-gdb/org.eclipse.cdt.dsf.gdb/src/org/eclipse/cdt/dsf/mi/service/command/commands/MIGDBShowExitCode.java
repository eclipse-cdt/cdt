/*******************************************************************************
 * Copyright (c) 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIGDBShowExitCodeInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * 
 *-data-evaluate-expression $_exitcode
 * ^done,value="10"
 *
 *   Show the current value of a $_exitcode
 *   
 *  With GDB 7.3, the exit code is provided by the MI =thread-group-exited event,
 *  which allows to handle multi-process situations.
 * 
 */
public class MIGDBShowExitCode extends MIDataEvaluateExpression<MIGDBShowExitCodeInfo> {

    /**
     * @since 1.1
     */
    public MIGDBShowExitCode(ICommandControlDMContext ctx) {
        super(ctx, "$_exitcode"); //$NON-NLS-1$
    }
    
    @Override
    public MIGDBShowExitCodeInfo getResult(MIOutput output) {
        return new MIGDBShowExitCodeInfo(output);
    }
}
