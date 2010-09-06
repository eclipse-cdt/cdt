/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.prvalueType;

import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.ASTTypeIdInitializerExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * C++ variant of type id initializer expression. type-id { initializer }
 */
public class CPPASTTypeIdInitializerExpression extends ASTTypeIdInitializerExpression {

	private CPPASTTypeIdInitializerExpression() {
	}

	public CPPASTTypeIdInitializerExpression(IASTTypeId typeId, IASTInitializer initializer) {
		super(typeId, initializer);
	}

	public IASTTypeIdInitializerExpression copy() {
		CPPASTTypeIdInitializerExpression expr= new CPPASTTypeIdInitializerExpression();
		initializeCopy(expr);
		return expr;
	}
	
	public IType getExpressionType() {
		final IASTTypeId typeId = getTypeId();
		return prvalueType(CPPVisitor.createType(typeId.getAbstractDeclarator()));
	}
}
