/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Mike Kucera (IBM)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;
import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.PRVALUE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.*;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;


public class CPPASTBinaryExpression extends ASTNode implements ICPPASTBinaryExpression, IASTAmbiguityParent {
	private int op;
    private IASTExpression operand1;
    private IASTInitializerClause operand2;
    private IType type;
    private ICPPFunction overload= UNINITIALIZED_FUNCTION;
    private IASTImplicitName[] implicitNames = null;

    public CPPASTBinaryExpression() {
	}

	public CPPASTBinaryExpression(int op, IASTExpression operand1, IASTInitializerClause operand2) {
		this.op = op;
		setOperand1(operand1);
		setInitOperand2(operand2);
	}

	@Override
	public CPPASTBinaryExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}
	
	@Override
	public CPPASTBinaryExpression copy(CopyStyle style) {
		CPPASTBinaryExpression copy = new CPPASTBinaryExpression();
		copy.op = op;
		copy.setOperand1(operand1 == null ? null : operand1.copy(style));
		copy.setInitOperand2(operand2 == null ? null : operand2.copy(style));
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
	public IASTExpression getOperand1() {
        return operand1;
    }

    @Override
	public IASTInitializerClause getInitOperand2() {
    	return operand2;
    }

    @Override
	public IASTExpression getOperand2() {
    	if (operand2 instanceof IASTExpression)
    		return (IASTExpression) operand2;
    	return null;
    }

    @Override
	public void setOperator(int op) {
        assertNotFrozen();
        this.op = op;
    }

    @Override
	public void setOperand1(IASTExpression expression) {
        assertNotFrozen();
        operand1 = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(OPERAND_ONE);
		}
    }

    public void setInitOperand2(IASTInitializerClause operand) {
        assertNotFrozen();
        operand2 = operand;
        if (operand != null) {
        	operand.setParent(this);
        	operand.setPropertyInParent(OPERAND_TWO);
		}
    }

    @Override
	public void setOperand2(IASTExpression expression) {
    	setInitOperand2(expression);
    }

    /**
     * @see org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner#getImplicitNames()
     */
	@Override
	public IASTImplicitName[] getImplicitNames() {
		if (implicitNames == null) {
			ICPPFunction overload = getOverload();
			if (overload == null || (overload instanceof CPPImplicitFunction && !(overload instanceof ICPPMethod))) {
				implicitNames = IASTImplicitName.EMPTY_NAME_ARRAY;
			} else {
				CPPASTImplicitName operatorName = new CPPASTImplicitName(overload.getNameCharArray(), this);
				operatorName.setBinding(overload);
				operatorName.setOperator(true);
				operatorName.computeOperatorOffsets(operand1, true);
				implicitNames = new IASTImplicitName[] { operatorName };
			}
		}

		return implicitNames;
	}

    @Override
	public boolean accept(ASTVisitor action) {
    	if (operand1 instanceof IASTBinaryExpression || operand2 instanceof IASTBinaryExpression) {
    		return acceptWithoutRecursion(this, action);
    	}
    	
		if (action.shouldVisitExpressions) {
			switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP:  return true;
	            default: break;
	        }
		}

		if (operand1 != null && !operand1.accept(action))
			return false;

		if (action.shouldVisitImplicitNames) {
			for (IASTImplicitName name : getImplicitNames()) {
				if (!name.accept(action))
					return false;
			}
		}

		if (operand2 != null && !operand2.accept(action))
			return false;

		if (action.shouldVisitExpressions && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;

		return true;
    }

    private static class N {
		final IASTBinaryExpression fExpression;
		int fState;
		N fNext;

		N(IASTBinaryExpression expr) {
			fExpression = expr;
		}
	}

	public static boolean acceptWithoutRecursion(IASTBinaryExpression bexpr, ASTVisitor action) {
		N stack= new N(bexpr);
		while (stack != null) {
			IASTBinaryExpression expr= stack.fExpression;
			if (stack.fState == 0) {
				if (action.shouldVisitExpressions) {
					switch (action.visit(expr)) {
					case ASTVisitor.PROCESS_ABORT:
						return false;
					case ASTVisitor.PROCESS_SKIP:
						stack= stack.fNext;
						continue;
					}
				}
				stack.fState= 1;
				IASTExpression op1 = expr.getOperand1();
				if (op1 instanceof IASTBinaryExpression) {
					N n= new N((IASTBinaryExpression) op1);
					n.fNext= stack;
					stack= n;
					continue;
				}
				if (op1 != null && !op1.accept(action))
					return false;
			}
			if (stack.fState == 1) {
				if (action.shouldVisitImplicitNames) {
					for (IASTImplicitName name : ((IASTImplicitNameOwner) expr).getImplicitNames()) {
		        		if (!name.accept(action))
		        			return false;
		        	}
		        }
				stack.fState= 2;

				IASTExpression op2 = expr.getOperand2();
				if (op2 instanceof IASTBinaryExpression) {
					N n= new N((IASTBinaryExpression) op2);
					n.fNext= stack;
					stack= n;
					continue;
				}
				if (op2 != null && !op2.accept(action))
					return false;
			}
			
			if (action.shouldVisitExpressions && action.leave(expr) == ASTVisitor.PROCESS_ABORT)
				return false;
		
			stack= stack.fNext;
		}
		
		return true;
	}

	@Override
	public void replace(IASTNode child, IASTNode other) {
		if (child == operand1) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			operand1 = (IASTExpression) other;
		}
		if (child == operand2) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			operand2 = (IASTInitializerClause) other;
		}
	}

    @Override
	public IType getExpressionType() {
    	if (type == null) {
    		type= createExpressionType();
    	}
    	return type;
    }

    @Override
	public ICPPFunction getOverload() {
    	if (overload != UNINITIALIZED_FUNCTION)
    		return overload;
    	
    	return overload = CPPSemantics.findOverloadedOperator(this);
    }

    @Override
	public ValueCategory getValueCategory() {
    	ICPPFunction op = getOverload();
		if (op != null) {
			return valueCategoryFromFunctionCall(op);
		}
		switch (getOperator()) {
		case op_assign:
		case op_binaryAndAssign:
		case op_binaryOrAssign:
		case op_binaryXorAssign:
		case op_divideAssign:
		case op_minusAssign:
		case op_moduloAssign:
		case op_multiplyAssign:
		case op_plusAssign:
		case op_shiftLeftAssign:
		case op_shiftRightAssign:
			return LVALUE;

		case op_pmdot:
			if (!(getExpressionType() instanceof ICPPFunctionType)) {
				return operand1.getValueCategory();
			}
			return PRVALUE;
			
		case op_pmarrow:
			if (!(getExpressionType() instanceof ICPPFunctionType)) 
				return LVALUE;
			return PRVALUE;
		}
		
		return PRVALUE;
    }
    
	@Override
	public boolean isLValue() {
		return getValueCategory() == LVALUE;
	}
	
	private IType createExpressionType() {
		// Check for overloaded operator.
		ICPPFunction o= getOverload();
		if (o != null) {
			return typeFromFunctionCall(o);
		}
		
        final int op = getOperator();
		IType type1 = prvalueType(operand1.getExpressionType());
		if (type1 instanceof ISemanticProblem) {
			return type1;
		}
		
		IType type2 = null;
		if (operand2 instanceof IASTExpression) {
			type2= prvalueType(((IASTExpression) operand2).getExpressionType());
			if (type2 instanceof ISemanticProblem) {
				return type2;
			}
		}
		
    	IType type= CPPArithmeticConversion.convertCppOperandTypes(op, type1, type2);
    	if (type != null) {
    		return type;
    	}

        switch (op) {
        case IASTBinaryExpression.op_lessEqual:
        case IASTBinaryExpression.op_lessThan:
        case IASTBinaryExpression.op_greaterEqual:
        case IASTBinaryExpression.op_greaterThan:
        case IASTBinaryExpression.op_logicalAnd:
        case IASTBinaryExpression.op_logicalOr:
        case IASTBinaryExpression.op_equals:
        case IASTBinaryExpression.op_notequals:
        	return CPPBasicType.BOOLEAN;

        case IASTBinaryExpression.op_plus:
        	if (type1 instanceof IPointerType) {
        		return type1;
        	} 
        	if (type2 instanceof IPointerType) {
        		return type2;
        	} 
        	break;

        case IASTBinaryExpression.op_minus:
        	if (type1 instanceof IPointerType) {
            	if (type2 instanceof IPointerType) {
            		return CPPVisitor.getPointerDiffType(this);
        		}
        		return type1;
        	}
        	break;

        case ICPPASTBinaryExpression.op_pmarrow:
        case ICPPASTBinaryExpression.op_pmdot:
        	if (type2 instanceof ICPPPointerToMemberType) {
        		IType t= ((ICPPPointerToMemberType) type2).getType();
        		if (t instanceof ICPPFunctionType)
        			return t;
        		if (op == ICPPASTBinaryExpression.op_pmdot && operand1.getValueCategory() == PRVALUE) {
        			return prvalueType(t);
        		}
        		return glvalueType(t);
        	}
    		return new ProblemType(ISemanticProblem.TYPE_UNKNOWN_FOR_EXPRESSION);
        }
        return type1;
	}
}
