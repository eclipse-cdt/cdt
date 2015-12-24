/*******************************************************************************
 * Copyright (c) 2004, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
