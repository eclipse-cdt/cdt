/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarInfoPathExpressionInfo;

/**
 *
 *     -var-info-path-expression NAME
 *
 *     as of GDB 6.7
 *
 *  Print full expression that this variable object represents:
 *
 *     (gdb) -var-info-path-expression C.Base.public.m_size
 *     ^done,path_expr=((Base)c).m_size)
 *
 *  Cannot be used for dynamic varobjs, or varobjs that have a dynamic varobj
 *  as ancestor.
 */

public class MIVarInfoPathExpression extends MICommand<MIVarInfoPathExpressionInfo> {
	/**
	 * @since 1.1
	 */
	public MIVarInfoPathExpression(ICommandControlDMContext dmc, String name) {
		super(dmc, "-var-info-path-expression", new String[] { name }); //$NON-NLS-1$
	}

	@Override
	public MIVarInfoPathExpressionInfo getResult(MIOutput out) {
		return new MIVarInfoPathExpressionInfo(out);
	}
}
