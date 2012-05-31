/*******************************************************************************
 * Copyright (c) 2009, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IProblemType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerClause;
import org.eclipse.cdt.internal.core.dom.parser.ASTTypeIdInitializerExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalTypeId;

/**
 * C++ variant of type id initializer expression. type-id { initializer }
 */
public class CPPASTTypeIdInitializerExpression extends ASTTypeIdInitializerExpression implements ICPPASTExpression {

	private ICPPEvaluation fEvaluation;

	private CPPASTTypeIdInitializerExpression() {
	}

	public CPPASTTypeIdInitializerExpression(IASTTypeId typeId, IASTInitializer initializer) {
		super(typeId, initializer);
	}

	@Override
	public IASTTypeIdInitializerExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}
	
	@Override
	public IASTTypeIdInitializerExpression copy(CopyStyle style) {
		CPPASTTypeIdInitializerExpression expr = new CPPASTTypeIdInitializerExpression();
		initializeCopy(expr, style);
		return expr;
	}

	@Override
	public ICPPEvaluation getEvaluation() {
		if (fEvaluation == null) 
			fEvaluation= computeEvaluation();
		
		return fEvaluation;
	}
	
	private ICPPEvaluation computeEvaluation() {
		final IASTInitializer initializer = getInitializer();
		if (!(initializer instanceof ICPPASTInitializerClause))
			return EvalFixed.INCOMPLETE;
		
		IType type= CPPVisitor.createType(getTypeId());
		if (type == null || type instanceof IProblemType)
			return EvalFixed.INCOMPLETE;
		
		return new EvalTypeId(type, ((ICPPASTInitializerClause) initializer).getEvaluation());
	}

    @Override
	public IType getExpressionType() {
    	return getEvaluation().getTypeOrFunctionSet(this);
    }
    
	@Override
	public ValueCategory getValueCategory() {
    	return getEvaluation().getValueCategory(this);
	}
}
