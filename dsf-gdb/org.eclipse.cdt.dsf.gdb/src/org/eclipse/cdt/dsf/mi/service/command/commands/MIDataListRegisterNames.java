/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
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

import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataListRegisterNamesInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * 
 *     -data-list-register-names [ ( REGNO )+ ]
 *
 *   Show a list of register names for the current target.  If no
 * arguments are given, it shows a list of the names of all the registers.
 * If integer numbers are given as arguments, it will print a list of the
 * names of the registers corresponding to the arguments.  To ensure
 * consistency between a register name and its number, the output list may
 * include empty register names.
 *
 */
public class MIDataListRegisterNames extends MICommand<MIDataListRegisterNamesInfo> 
{
    public MIDataListRegisterNames(IContainerDMContext ctx) {
        super(ctx, "-data-list-register-names"); //$NON-NLS-1$
    }

    public MIDataListRegisterNames(IContainerDMContext ctx, int [] regnos) {
        this(ctx);
        if (regnos != null && regnos.length > 0) {
            String[] array = new String[regnos.length];
            for (int i = 0; i < regnos.length; i++) {
                array[i] = Integer.toString(regnos[i]);
            }
            setParameters(array);
        }
    }
    
    @Override
    public MIDataListRegisterNamesInfo getResult(MIOutput output) {
        return new MIDataListRegisterNamesInfo(output);
    }
}
