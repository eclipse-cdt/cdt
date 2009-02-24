/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying masterials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *    Sergey Prigogin (Google)
 *    Mike Kucera (IBM)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.*;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

/**
 * Unary expression in c++
 */
public class CPPASTUnaryExpression extends ASTNode implements ICPPASTUnaryExpression, IASTAmbiguityParent {
    private int op;
    private IASTExpression operand;
    
    public CPPASTUnaryExpression() {
	}

	public CPPASTUnaryExpression(int operator, IASTExpression operand) {
		op = operator;
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

    public void setOperator(int operator) {
        assertNotFrozen();
        op = operator;
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
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitExpressions) {
		    switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP:  return true;
	            default : break;
	        }
		}
      
        if (operand != null && !operand.accept(action)) return false;
        
        if (action.shouldVisitExpressions) {
		    switch (action.leave(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP:  return true;
	            default : break;
	        }
		}
        return true;
    }

    public void replace(IASTNode child, IASTNode other) {
        if (child == operand) {
            other.setPropertyInParent(child.getPropertyInParent());
            other.setParent(child.getParent());
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
		
		final IASTExpression operand = getOperand();

		if (op == IASTUnaryExpression.op_amper) {  // check for pointer to member
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
								if (!inParenthesis) {
									return new CPPPointerToMemberType(member.getType(), member.getClassOwner(), false, false);
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

			IType type= operand.getExpressionType();
			type = SemanticUtil.getNestedType(type, TDEF | REF);
			
			IType operator = findOperatorReturnType(type);
			if(operator != null)
				return operator;

			return new CPPPointerType(type);
		} 
		
		
		if (op == IASTUnaryExpression.op_star) {
			IType type= operand.getExpressionType();
			type = SemanticUtil.getNestedType(type, TDEF | REF | CVQ);
	    	if (type instanceof IProblemBinding) {
	    		return type;
	    	}
		    try {
		    	IType operator = findOperatorReturnType(type);
				if(operator != null) {
					return operator;
				} else if (type instanceof IPointerType || type instanceof IArrayType) {
					return ((ITypeContainer) type).getType();
				} else if (type instanceof ICPPUnknownType) {
					return CPPUnknownClass.createUnnamedInstance();
				}
			} catch (DOMException e) {
				return e.getProblem();
			}
			return new ProblemBinding(this, IProblemBinding.SEMANTIC_INVALID_TYPE, this.getRawSignature().toCharArray());
		} 
		

		IType type= operand.getExpressionType();
		type = SemanticUtil.getNestedType(type, TDEF | REF);
		IType operator = findOperatorReturnType(type);
		if(operator != null) {
			return operator;
		}
		
		if(op == IASTUnaryExpression.op_not) {
			return new CPPBasicType(ICPPBasicType.t_bool, 0);
		}
		if (type instanceof CPPBasicType) {
			((CPPBasicType) type).setFromExpression(this);
		}
		return type;
    }
    
    
    private IType findOperatorReturnType(IType type) {
    	ICPPFunction operatorFunction = findOperatorFunction(type);
    	if(operatorFunction != null) {
    		try {
				return operatorFunction.getType().getReturnType();
			} catch (DOMException e) {
				return e.getProblem();
			}
    	}
    	return null;
    }
    
    
    private ICPPFunction findOperatorFunction(IType type) {
    	if(type instanceof ICPPClassType) {
			ICPPFunction operator = CPPSemantics.findOperator(this, (ICPPClassType) type);
			if(operator != null)
				return operator;
			return CPPSemantics.findOverloadedOperator(this); 
		}
    	
    	return null;
    }
}
