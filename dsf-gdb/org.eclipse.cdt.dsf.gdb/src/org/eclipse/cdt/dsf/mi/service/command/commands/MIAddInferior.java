/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIAddInferiorInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;


/**	
 *   -add-inferior
 *   ^done,inferior="i2"
 *   
 *   Creates a new inferior. The created inferior is not associated with any executable. 
 *   Such association may be established with the '-file-exec-and-symbols' command.
 *   The command response has a single field, 'thread-group', whose value is the 
 *   identifier of the thread group corresponding to the new inferior.
 *   
 *   @since 4.0
 */
public class MIAddInferior extends MICommand<MIAddInferiorInfo>
{
    public MIAddInferior(ICommandControlDMContext dmc) {
        super(dmc, "-add-inferior"); //$NON-NLS-1$
    }
    
    @Override
    public MIAddInferiorInfo getResult(MIOutput output) {
        return new MIAddInferiorInfo(output);
    }
}
