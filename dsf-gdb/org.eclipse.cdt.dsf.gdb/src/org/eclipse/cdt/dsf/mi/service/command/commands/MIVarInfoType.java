/*******************************************************************************
 * Copyright (c) 2009 QNX Software Systems and others.
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
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarInfoTypeInfo;

/**
 *
 *     -var-info-type NAME
 *
 *   Returns the type of the specified variable NAME.  The type is
 * returned as a string in the same format as it is output by the GDB CLI:
 *
 *     type=TYPENAME
 *
 */
public class MIVarInfoType extends MICommand<MIVarInfoTypeInfo> {
	/**
	 * @since 1.1
	 */
	public MIVarInfoType(ICommandControlDMContext ctx, String name) {
		super(ctx, "-var-info-type", new String[] { name }); //$NON-NLS-1$
	}

	@Override
	public MIVarInfoTypeInfo getResult(MIOutput out) {
		return new MIVarInfoTypeInfo(out);
	}
}
