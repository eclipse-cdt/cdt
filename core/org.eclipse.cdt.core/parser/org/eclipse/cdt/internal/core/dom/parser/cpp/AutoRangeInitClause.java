/*******************************************************************************
 * Copyright (c) 2021 Advantest Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.IAutoRangeIntitClause;

public class AutoRangeInitClause extends CPPASTUnaryExpression implements IAutoRangeIntitClause {

	private IType fallbackType;

	public AutoRangeInitClause(int opStar, IASTExpression beginExpr) {
		super(opStar, beginExpr);
	}

	@Override
	public void setFallbackType(IType type) {
		this.fallbackType = type;
	}

	@Override
	public IType getFallbackType() {
		return fallbackType;
	}

}
