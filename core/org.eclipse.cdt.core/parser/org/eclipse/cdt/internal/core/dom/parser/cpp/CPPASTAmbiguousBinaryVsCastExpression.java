/*******************************************************************************
 * Copyright (c) 2008, 2015 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - Initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.internal.core.dom.parser.ASTAmbiguousBinaryVsCastExpression;

public class CPPASTAmbiguousBinaryVsCastExpression extends ASTAmbiguousBinaryVsCastExpression
		implements ICPPASTExpression {

	public CPPASTAmbiguousBinaryVsCastExpression(IASTBinaryExpression bexp, IASTCastExpression castExpr) {
		super(bexp, castExpr);
	}

	@Override
	public IASTImplicitDestructorName[] getImplicitDestructorNames() {
		throw new UnsupportedOperationException();
	}
}
