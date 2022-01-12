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
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *     Ericsson				- Modified for handling of frame contexts
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarEvaluateExpressionInfo;

/**
 *
 *     -var-evaluate-expression NAME
 *
 *  Evaluates the expression that is represented by the specified
 * variable object and returns its value as a string in the current format
 * specified for the object:
 *
 *      value=VALUE
 *
 */
public class MIVarEvaluateExpression extends MICommand<MIVarEvaluateExpressionInfo> {

	/**
	 * @since 1.1
	 */
	public MIVarEvaluateExpression(ICommandControlDMContext dmc, String name) {
		super(dmc, "-var-evaluate-expression", new String[] { name }); //$NON-NLS-1$
	}

	@Override
	public MIVarEvaluateExpressionInfo getResult(MIOutput out) {
		return new MIVarEvaluateExpressionInfo(out);
	}
}
