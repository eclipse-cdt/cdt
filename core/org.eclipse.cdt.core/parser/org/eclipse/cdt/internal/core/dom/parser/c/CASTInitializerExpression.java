/*******************************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 * Yuan Zhang / Beth Tibbitts (IBM Research)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;

@Deprecated
public class CASTInitializerExpression extends CASTEqualsInitializer implements IASTInitializerExpression {

    public CASTInitializerExpression() {
	}

	public CASTInitializerExpression(IASTExpression expression) {
		setExpression(expression);
	}

	@Override
	public CASTInitializerExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTInitializerExpression copy(CopyStyle style) {
		CASTInitializerExpression copy = new CASTInitializerExpression();
		IASTInitializerClause init = getInitializerClause();
		copy.setInitializerClause(init == null ? null : init.copy(style));
		return copy(copy, style);
	}
}
