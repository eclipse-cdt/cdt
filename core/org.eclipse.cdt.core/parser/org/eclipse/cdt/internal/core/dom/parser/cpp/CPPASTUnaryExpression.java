/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying masterials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *    Sergey Prigogin (Google)
 *    Mike Kucera (IBM)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;
import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.PRVALUE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.*;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.getUltimateTypeUptoPointers;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * Unary expression in c++
 */
public class CPPASTUnaryExpression extends ASTNode implements ICPPASTUnaryExpression, IASTAmbiguityParent {
	private int op;
    private IASTExpression operand;

    private ICPPFunction overload = UNINITIALIZED_FUNCTION;
    private IASTImplicitName[] implicitNames = null;

    public CPPASTUnaryExpression() {
	}

	public CPPASTUnaryExpression(int operator, IASTExpression operand) {
		op = operator;
		setOperand(operand);
	}

	@Override
	public CPPASTUnaryExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}
	
	@Override
	public CPPASTUnaryExpression copy(CopyStyle style) {
		CPPASTUnaryExpression copy = new CPPASTUnaryExpression(op, operand == null ? null
				: operand.copy(style));
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

	@Override
	public int getOperator() {
        return op;
    }

    @Override
	public void setOperator(int operator) {
        assertNotFrozen();
        op = operator;
    }

    @Override
	public IASTExpression getOperand() {
        return operand;
    }

    @Override
	public void setOperand(IASTExpression expression) {
        assertNotFrozen();
        operand = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(OPERAND);
		}
    }

    public boolean isPostfixOperator() {
    	return op == op_postFixDecr || op == op_postFixIncr;
    }

    /**
     * @see org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner#getImplicitNames()
     */
    @Override
	public IASTImplicitName[] getImplicitNames() {
		if (implicitNames == null) {
			ICPPFunction overload = getOverload();
			if (overload == null || overload instanceof CPPImplicitFunction) {
				implicitNames = IASTImplicitName.EMPTY_NAME_ARRAY;
			} else {
				CPPASTImplicitName operatorName = new CPPASTImplicitName(overload.getNameCharArray(), this);
				operatorName.setOperator(true);
				operatorName.setBinding(overload);
				operatorName.computeOperatorOffsets(operand, isPostfixOperator());
				implicitNames = new IASTImplicitName[] { operatorName };
			}
		}
		
		return implicitNames;
	}
	
    @Override
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitExpressions) {
		    switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP:  return true;
	            default: break;
	        }
		}
 
        final boolean isPostfix = isPostfixOperator();

        if (!isPostfix && action.shouldVisitImplicitNames) {
        	for (IASTImplicitName name : getImplicitNames()) {
        		if (!name.accept(action))
        			return false;
        	}
        }

        if (operand != null && !operand.accept(action))
        	return false;

        if (isPostfix && action.shouldVisitImplicitNames) {
        	for (IASTImplicitName name : getImplicitNames()) {
        		if (!name.accept(action))
        			return false;
        	}
        }

        if (action.shouldVisitExpressions) {
		    switch (action.leave(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP:  return true;
	            default: break;
	        }
		}
        return true;
    }

    @Override
	public void replace(IASTNode child, IASTNode other) {
        if (child == operand) {
            other.setPropertyInParent(child.getPropertyInParent());
            other.setParent(child.getParent());
            operand  = (IASTExpression) other;
        }
    }

    public ICPPFunction getOverload() {
    	if (overload != UNINITIALIZED_FUNCTION)
    		return overload;
    	
    	overload = CPPSemantics.findOverloadedOperator(this);
    	if (overload != null && op == op_amper && computePointerToMemberType() instanceof CPPPointerToMemberType)
    		overload = null;
    	
    	return overload;
    }

    private IType computePointerToMemberType() {
    	IASTNode child= operand;
		boolean inParenthesis= false;
		while (child instanceof IASTUnaryExpression && ((IASTUnaryExpression) child).getOperator() == IASTUnaryExpression.op_bracketedPrimary) {
			child= ((IASTUnaryExpression) child).getOperand();
			inParenthesis= true;
		}
		if (child instanceof IASTIdExpression) {
			IASTName name= ((IASTIdExpression) child).getName();
			IBinding b= name.resolveBinding();
			if (b instanceof ICPPMember) {
				ICPPMember member= (ICPPMember) b;
				try {
					if (name instanceof ICPPASTQualifiedName) {
						if (!member.isStatic()) { // so if the member is static it will fall through
							overload= null;
							if (!inParenthesis) {
								return new CPPPointerToMemberType(member.getType(), member.getClassOwner(), false, false, false);
							} else if (member instanceof IFunction) {
								return new ProblemBinding(operand, IProblemBinding.SEMANTIC_INVALID_TYPE, operand.getRawSignature().toCharArray());
							}
						}
					}
				} catch (DOMException e) {
					return e.getProblem();
				}
			}
		}
		return null;
    }

    @Override
	public IType getExpressionType() {
    	final int op= getOperator();
		switch (op) {
		case op_sizeof:
		case op_sizeofParameterPack:
			return CPPVisitor.get_SIZE_T(this);
		case op_typeid:
			return CPPVisitor.get_type_info(this);
		case op_bracketedPrimary:
			return getOperand().getExpressionType();
		case op_throw:
			return CPPSemantics.VOID_TYPE;
		}
		
		final IASTExpression operand = getOperand();

		if (op == op_amper) {  // check for pointer to member
			IType ptm = computePointerToMemberType();
			if (ptm != null)
				return ptm;

	    	ICPPFunction overload = getOverload();
	    	if (overload != null)
	    		return typeFromFunctionCall(overload);

			return new CPPPointerType(operand.getExpressionType());
		}

    	ICPPFunction overload = getOverload();
    	if (overload != null)
    		return typeFromFunctionCall(overload);

		if (op == op_star) {
			IType type= operand.getExpressionType();
			type = prvalueType(type);
	    	if (type instanceof IPointerType) {
	    		type= ((ITypeContainer) type).getType();
	    		return glvalueType(type);
			} 
	    	if (type instanceof ISemanticProblem) {
	    		return type;
	    	}
	    	type= getUltimateTypeUptoPointers(type);
	    	if (type instanceof ICPPUnknownType) {
	    		// mstodo Type of unknown
				return CPPUnknownClass.createUnnamedInstance();
			}
			return new ProblemType(ISemanticProblem.TYPE_UNKNOWN_FOR_EXPRESSION);
		}

		IType typeOfOperand= operand.getExpressionType();

		switch (op) {
		case op_not:
			return CPPBasicType.BOOLEAN;
		case op_postFixDecr:
		case op_postFixIncr:
			typeOfOperand= prvalueType(typeOfOperand);
			break;
		case op_minus:
		case op_plus:
		case op_tilde:
			IType t= CPPArithmeticConversion.promoteCppType(prvalueType(typeOfOperand));
			if (t != null) {
				return t;
			}
			break;
		}

		if (typeOfOperand instanceof CPPBasicType) {
			((CPPBasicType) typeOfOperand).setFromExpression(this);
		}
		return typeOfOperand;
    }

    @Override
	public ValueCategory getValueCategory() {
    	final int op= getOperator();
		switch (op) {
		case op_typeid:
			return LVALUE;
		case op_sizeof:
		case op_sizeofParameterPack:
			return PRVALUE;
		case op_bracketedPrimary:
			return (operand).getValueCategory();
		}
		
		if (op == op_amper && computePointerToMemberType() != null) {
			return PRVALUE;
		}

		ICPPFunction overload = getOverload();
    	if (overload != null)
    		return valueCategoryFromFunctionCall(overload);

    	switch(op) {
    	case op_star:
    	case op_prefixDecr:
    	case op_prefixIncr:
			return LVALUE;
    	}
		return PRVALUE;
    }
    
	@Override
	public boolean isLValue() {
		return getValueCategory() == LVALUE;
	}
}
