/*******************************************************************************
 * Copyright (c) 2023 Igor V. Kovalenko.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Igor V. Kovalenko - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * Represents <code>...</code> token in fold expression.
 */
public class CPPASTFoldExpressionToken extends ASTNode implements IASTExpression {

	@Override
	public IType getExpressionType() {
		return null;
	}

	@Override
	public boolean isLValue() {
		return false;
	}

	@Override
	public ValueCategory getValueCategory() {
		return null;
	}

	@Override
	public IASTExpression copy() {
		return null;
	}

	@Override
	public IASTExpression copy(CopyStyle style) {
		return null;
	}
}
