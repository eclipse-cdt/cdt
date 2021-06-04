/*******************************************************************************
 * Copyright (c) 2020, 2029 Ashling Microsystems.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vinod Appu(Ashling Microsystems) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIGlobalVariableInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * -symbol-info-variables
 * Returns the list of global variables for the current execution context.
 *
 */
public class MIInfoVariables extends MICommand<MIGlobalVariableInfo> {

	public MIInfoVariables(IDMContext ctx) {
		super(ctx, "-symbol-info-variables"); //$NON-NLS-1$
	}

	@Override
	public MIInfo getResult(MIOutput MIresult) {
		return new MIGlobalVariableInfo(MIresult);
	}

}