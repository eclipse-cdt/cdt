/*******************************************************************************
 * Copyright (c) 2004, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;

@Deprecated
public class CPPASTInitializerExpression extends CPPASTEqualsInitializer implements IASTInitializerExpression {

	public CPPASTInitializerExpression() {
	}

	public CPPASTInitializerExpression(IASTExpression expression) {
		setExpression(expression);
	}

	@Override
	public CPPASTInitializerExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTInitializerExpression copy(CopyStyle style) {
		CPPASTInitializerExpression copy = new CPPASTInitializerExpression();
		IASTInitializerClause init = getInitializerClause();
		copy.setInitializerClause(init == null ? null : init.copy(style));
		return copy(copy, style);
	}
}
