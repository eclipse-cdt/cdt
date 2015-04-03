/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *    Yuan Zhang / Beth Tibbitts (IBM Research)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression;
import org.eclipse.cdt.internal.core.dom.parser.ASTTypeIdInitializerExpression;

/**
 * C-specific implementation adds nothing but the c-specific interface.
 */
public class CASTTypeIdInitializerExpression extends ASTTypeIdInitializerExpression implements
        ICASTTypeIdInitializerExpression {

	private CASTTypeIdInitializerExpression() {
		super();
	}

	public CASTTypeIdInitializerExpression(IASTTypeId typeId, IASTInitializer initializer) {
		super(typeId, initializer);
	}

	@Override
	public CASTTypeIdInitializerExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTTypeIdInitializerExpression copy(CopyStyle style) {
		CASTTypeIdInitializerExpression copy = new CASTTypeIdInitializerExpression();
		initializeCopy(copy, style);
		return copy;
	}

	@Override
	public IType getExpressionType() {
		return CVisitor.createType(getTypeId().getAbstractDeclarator());
	}
}
