/*******************************************************************************
 * Copyright (c) 2019 Marco Stornelli
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCastExpression;

public class QuickFixCppCastStatic extends AbstractQuickFixCppCast {

	@Override
	public String getLabel() {
		return QuickFixMessages.QuickFixCppCast_static_cast;
	}

	@Override
	protected int getCastType() {
		return ICPPASTCastExpression.op_static_cast;
	}

}
