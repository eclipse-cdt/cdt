/*******************************************************************************
 * Copyright (c) 2007, 2009 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson           - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.ExprMetaGetVarInfo;

public class ExprMetaGetVar extends ExprMetaCommand<ExprMetaGetVarInfo> {

	public ExprMetaGetVar(IExpressionDMContext ctx) {
		super(ctx);
	}
}
