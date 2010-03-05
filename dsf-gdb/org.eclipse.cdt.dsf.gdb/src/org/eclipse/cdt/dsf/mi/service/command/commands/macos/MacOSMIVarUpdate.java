/*******************************************************************************
 * Copyright (c) 2010 QNX Software Systems and others.
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

package org.eclipse.cdt.dsf.mi.service.command.commands.macos;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIVarUpdate;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarUpdateInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.macos.MacOSMIVarUpdateInfo;

/**
 * 
 *     -var-update [print-values] {NAME | "*"}
 *
 *  Update the value of the variable object NAME by evaluating its
 *  expression after fetching all the new values from memory or registers.
 *  A `*' causes all existing variable objects to be updated.
 *  If print-values has a value for of 0 or --no-values, print only the names of the variables; 
 *  if print-values is 1 or --all-values, also print their values; 
 *  if it is 2 or --simple-values print the name and value for simple data types and just 
 *  the name for arrays, structures and unions. 
 *  
 *  It seems that for MacOS, we must use the full string for print-values, such as
 *  --all-values.
 * 
 * @since 3.0
 */
public class MacOSMIVarUpdate extends MIVarUpdate {

	public MacOSMIVarUpdate(ICommandControlDMContext dmc, String name) {
		super(dmc, name);
		// Must use --all-values instead of 1 for Mac OS
		setParameters(new String[] { "--all-values", name }); //$NON-NLS-1$
	}
	
    @Override
    public MIVarUpdateInfo getResult(MIOutput out) {
        return new MacOSMIVarUpdateInfo(out);
    }
}
