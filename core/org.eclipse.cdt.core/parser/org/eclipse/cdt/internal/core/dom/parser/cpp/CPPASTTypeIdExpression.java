/*******************************************************************************
 *  Copyright (c) 2004, 2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      John Camelon (IBM) - Initial API and implementation
 *      Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IProblemType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalUnaryTypeID;

public class CPPASTTypeIdExpression extends ASTNode implements ICPPASTTypeIdExpression {
    private int op;
    private IASTTypeId typeId;
	private ICPPEvaluation fEvaluation;

    public CPPASTTypeIdExpression() {
	}

	public CPPASTTypeIdExpression(int op, IASTTypeId typeId) {
		this.op = op;
		setTypeId(typeId);
	}

	@Override
	public CPPASTTypeIdExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTTypeIdExpression copy(CopyStyle style) {
		CPPASTTypeIdExpression copy =
				new CPPASTTypeIdExpression(op, typeId == null ? null : typeId.copy(style));
		return copy(copy, style);
	}

	@Override
	public int getOperator() {
        return op;
    }

    @Override
	public void setOperator(int value) {
        assertNotFrozen();
        this.op = value;
    }

    @Override
	public void setTypeId(IASTTypeId typeId) {
        assertNotFrozen();
        this.typeId = typeId;
        if (typeId != null) {
        	typeId.setParent(this);
        	typeId.setPropertyInParent(TYPE_ID);
        }
    }

    @Override
	public IASTTypeId getTypeId() {
        return typeId;
    }

    @Override
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitExpressions) {
		    switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}

        if (typeId != null && !typeId.accept(action)) return false;

        if (action.shouldVisitExpressions) {
		    switch (action.leave(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
        return true;
    }

	@Override
	public ICPPEvaluation getEvaluation() {
		if (fEvaluation == null) {
			IType type= CPPVisitor.createType(typeId);
			if (type == null || type instanceof IProblemType) {
				fEvaluation= EvalFixed.INCOMPLETE;
			} else {
				fEvaluation= new EvalUnaryTypeID(op, type);
			}
		}
		return fEvaluation;
	}

    @Override
	public IType getExpressionType() {
    	return getEvaluation().getTypeOrFunctionSet(this);
    }

	@Override
	public ValueCategory getValueCategory() {
		return getEvaluation().getValueCategory(this);
	}

	@Override
	public boolean isLValue() {
		return getValueCategory() == LVALUE;
	}
}
