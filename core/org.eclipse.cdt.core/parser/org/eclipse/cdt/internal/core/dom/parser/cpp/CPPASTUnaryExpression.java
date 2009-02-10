/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

/**
 * @author jcamelon
 */
public class CPPASTUnaryExpression extends ASTNode implements
        ICPPASTUnaryExpression, IASTAmbiguityParent {

    private int op;
    private IASTExpression operand;

    
    public CPPASTUnaryExpression() {
	}

	public CPPASTUnaryExpression(int operator, IASTExpression operand) {
		this.op = operator;
		setOperand(operand);
	}

	public CPPASTUnaryExpression copy() {
		CPPASTUnaryExpression copy = new CPPASTUnaryExpression(op, operand == null ? null : operand.copy());
		copy.setOffsetAndLength(this);
		return copy;
	}
	
	public int getOperator() {
        return op;
    }

    public void setOperator(int value) {
        assertNotFrozen();
        this.op = value;
    }

    public IASTExpression getOperand() {
        return operand;
    }

    public void setOperand(IASTExpression expression) {
        assertNotFrozen();
        operand = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(OPERAND);
		}
    }

    @Override
	public boolean accept( ASTVisitor action ){
        if( action.shouldVisitExpressions ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
      
        if( operand != null ) if( !operand.accept( action ) ) return false;
        
        if( action.shouldVisitExpressions ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }

    public void replace(IASTNode child, IASTNode other) {
        if( child == operand ) {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            operand  = (IASTExpression) other;
        }
        
    }
    
    public IType getExpressionType() {
    	final int op= getOperator();
		switch (op) {
		case IASTUnaryExpression.op_sizeof:
			return CPPVisitor.get_SIZE_T(this);
		case IASTUnaryExpression.op_typeid:
			return CPPVisitor.get_type_info(this);
		}
		
		IType type= getOperand().getExpressionType();
		type = SemanticUtil.getUltimateTypeViaTypedefs(type);

		if (op == IASTUnaryExpression.op_star) {
		    try {
		    	type = SemanticUtil.getUltimateTypeUptoPointers(type);
		    	if (type instanceof IProblemBinding) {
		    		return type;
		    	}
				if (type instanceof ICPPClassType) {
					ICPPFunction operator= CPPSemantics.findOperator(this, (ICPPClassType) type);
					if (operator != null) {
						return operator.getType().getReturnType();
					}
				} else if (type instanceof IPointerType || type instanceof IArrayType) {
					return ((ITypeContainer) type).getType();
				} else if (type instanceof ICPPUnknownType) {
					return CPPUnknownClass.createUnnamedInstance();
				}
				return new ProblemBinding(this, IProblemBinding.SEMANTIC_INVALID_TYPE,
						this.getRawSignature().toCharArray());
			} catch (DOMException e) {
				return e.getProblem();
			}
		} else if (op == IASTUnaryExpression.op_amper) {
			if (type instanceof ICPPReferenceType) {
				try {
					type = ((ICPPReferenceType) type).getType();
				} catch (DOMException e) {
				}
			}
			if (type instanceof ICPPFunctionType) {
				ICPPFunctionType functionType = (ICPPFunctionType) type;
				IPointerType thisType = functionType.getThisType();
				if (thisType != null) {
					IType nestedType;
					try {
						nestedType = thisType.getType();
						while (nestedType instanceof ITypeContainer) {
							nestedType = ((ITypeContainer) nestedType).getType();
						}
					} catch (DOMException e) {
						return e.getProblem();
					}
					return new CPPPointerToMemberType(type, nestedType, thisType.isConst(), thisType
							.isVolatile());
				}
			}
			return new CPPPointerType(type);
		} else if (type instanceof CPPBasicType) {
			((CPPBasicType) type).setFromExpression(this);
		}
		return type;
    }
}
