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
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarInfoNumChildrenInfo;

/**
 *
 * -var-info-num-children NAME
 *
 * Returns the number of children of a variable object NAME:
 *
 * numchild=N
 *
 * Note that this number is not completely reliable for a dynamic varobjs. It
 * will return the current number of children, but more children may be
 * available.
 */
public class MIVarInfoNumChildren extends MICommand<MIVarInfoNumChildrenInfo> {
	public MIVarInfoNumChildren(IExpressionDMContext ctx, String name) {
		super(ctx, "-var-info-num-children", new String[] { name }); //$NON-NLS-1$
	}

	@Override
	public MIVarInfoNumChildrenInfo getResult(MIOutput out) {
		return new MIVarInfoNumChildrenInfo(out);
	}
}
