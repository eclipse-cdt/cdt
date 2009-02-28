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
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarDeleteInfo;

/**
 * 
 *    -var-delete NAME
 *
 *  Deletes a previously created variable object and all of its children.
 *
 *  Returns an error if the object NAME is not found.
 * 
 */
public class MIVarDelete extends MICommand<MIVarDeleteInfo> 
{
    /**
     * @since 1.1
     */
    public MIVarDelete(ICommandControlDMContext dmc, String name) {
        super(dmc, "-var-delete", new String[]{name}); //$NON-NLS-1$
    }
    
    @Override
    public MIVarDeleteInfo getResult(MIOutput out) {
        return new MIVarDeleteInfo(out);
    }
}
