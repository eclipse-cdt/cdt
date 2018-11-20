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
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataEvaluateExpressionInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 *
 *      -data-evaluate-expression EXPR
 *
 *   Evaluate EXPR as an expression.  The expression could contain an
 *inferior function call.  The function call will execute synchronously.
 *If the expression contains spaces, it must be enclosed in double quotes.
 *
 */
public class MIDataEvaluateExpression<V extends MIDataEvaluateExpressionInfo> extends MICommand<V> {
	/**
	 * @since 1.1
	 */
	public MIDataEvaluateExpression(ICommandControlDMContext ctx, String expr) {
		super(ctx, "-data-evaluate-expression", new String[] { expr }); //$NON-NLS-1$
	}

	public MIDataEvaluateExpression(IMIExecutionDMContext execDmc, String expr) {
		super(execDmc, "-data-evaluate-expression", new String[] { expr }); //$NON-NLS-1$
	}

	public MIDataEvaluateExpression(IFrameDMContext frameDmc, String expr) {
		super(frameDmc, "-data-evaluate-expression", new String[] { expr }); //$NON-NLS-1$
	}

	public MIDataEvaluateExpression(IExpressionDMContext exprDmc) {
		super(exprDmc, "-data-evaluate-expression", new String[] { exprDmc.getExpression() }); //$NON-NLS-1$
	}

	@Override
	public MIDataEvaluateExpressionInfo getResult(MIOutput output) {
		return new MIDataEvaluateExpressionInfo(output);
	}
}
