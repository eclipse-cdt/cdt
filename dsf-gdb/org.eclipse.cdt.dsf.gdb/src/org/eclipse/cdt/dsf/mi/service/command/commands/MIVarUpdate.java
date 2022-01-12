/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *     Ericsson				- Modified for handling of frame contexts
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarUpdateInfo;

/**
 *
 *     -var-update [print-values] {NAME | "*"}
 *
 *  Update the value of the variable object NAME by evaluating its
 *  expression after fetching all the new values from memory or registers.
 *  A `*' causes all existing variable objects to be updated.
  * If print-values has a value for of 0 or --no-values, print only the names of the variables;
  * if print-values is 1 or --all-values, also print their values;
  * if it is 2 or --simple-values print the name and value for simple data types and just
  * the name for arrays, structures and unions.
 */
public class MIVarUpdate extends MICommand<MIVarUpdateInfo> {

	/**
	 * @since 1.1
	 */
	public MIVarUpdate(ICommandControlDMContext dmc, String name) {
		super(dmc, "-var-update", new String[] { "1", name }); //$NON-NLS-1$//$NON-NLS-2$
	}

	@Override
	public MIVarUpdateInfo getResult(MIOutput out) {
		return new MIVarUpdateInfo(out);
	}
}
