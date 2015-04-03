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
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalBinary;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;


public class CPPASTBinaryExpression extends ASTNode implements ICPPASTBinaryExpression, IASTAmbiguityParent {
	private int fOperator;
    private ICPPASTExpression fOperand1;
    private ICPPASTInitializerClause fOperand2;

    private ICPPEvaluation fEvaluation;
    private IASTImplicitName[] fImplicitNames;

    public CPPASTBinaryExpression() {
	}

	public CPPASTBinaryExpression(int op, IASTExpression operand1, IASTInitializerClause operand2) {
		this.fOperator = op;
		setOperand1(operand1);
		setInitOperand2(operand2);
	}

	@Override
	public CPPASTBinaryExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTBinaryExpression copy(CopyStyle style) {
		CPPASTBinaryExpression copy = new CPPASTBinaryExpression(fOperator,
				fOperand1 == null ? null : fOperand1.copy(style),
				fOperand2 == null ? null : fOperand2.copy(style));
		return copy(copy, style);
	}

	@Override
	public int getOperator() {
        return fOperator;
    }

    @Override
	public IASTExpression getOperand1() {
        return fOperand1;
    }

    @Override
	public IASTInitializerClause getInitOperand2() {
    	return fOperand2;
    }

    @Override
	public IASTExpression getOperand2() {
    	if (fOperand2 instanceof IASTExpression)
    		return (IASTExpression) fOperand2;
    	return null;
    }

    @Override
	public void setOperator(int op) {
        assertNotFrozen();
        this.fOperator = op;
    }

    @Override
	public void setOperand1(IASTExpression expression) {
        assertNotFrozen();
        if (expression != null) {
        	if (!(expression instanceof ICPPASTExpression))
        		throw new IllegalArgumentException(expression.getClass().getName());

			expression.setParent(this);
			expression.setPropertyInParent(OPERAND_ONE);
		}
        fOperand1 = (ICPPASTExpression) expression;
    }

    public void setInitOperand2(IASTInitializerClause operand) {
        assertNotFrozen();
        if (operand != null) {
        	if (!(operand instanceof ICPPASTInitializerClause))
        		throw new IllegalArgumentException(operand.getClass().getName());
        	operand.setParent(this);
        	operand.setPropertyInParent(OPERAND_TWO);
		}
        fOperand2 = (ICPPASTInitializerClause) operand;
    }

    @Override
	public void setOperand2(IASTExpression expression) {
    	setInitOperand2(expression);
    }

	@Override
	public IASTImplicitName[] getImplicitNames() {
		if (fImplicitNames == null) {
			ICPPFunction overload = getOverload();
			if (overload == null || (overload instanceof CPPImplicitFunction && !(overload instanceof ICPPMethod))) {
				fImplicitNames = IASTImplicitName.EMPTY_NAME_ARRAY;
			} else {
				CPPASTImplicitName operatorName = new CPPASTImplicitName(overload.getNameCharArray(), this);
				operatorName.setBinding(overload);
				operatorName.setOperator(true);
				operatorName.computeOperatorOffsets(fOperand1, true);
				fImplicitNames = new IASTImplicitName[] { operatorName };
			}
		}

		return fImplicitNames;
	}

    @Override
	public boolean accept(ASTVisitor action) {
    	if (fOperand1 instanceof IASTBinaryExpression || fOperand2 instanceof IASTBinaryExpression) {
    		return acceptWithoutRecursion(this, action);
    	}

		if (action.shouldVisitExpressions) {
			switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP:  return true;
	            default: break;
	        }
		}

		if (fOperand1 != null && !fOperand1.accept(action))
			return false;

		if (action.shouldVisitImplicitNames && !acceptByNodes(getImplicitNames(), action))
			return false;

		if (fOperand2 != null && !fOperand2.accept(action))
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

	private static boolean acceptWithoutRecursion(IASTBinaryExpression bexpr, ASTVisitor action) {
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
				if (action.shouldVisitImplicitNames &&
						!acceptByNodes(((IASTImplicitNameOwner) expr).getImplicitNames(), action)) {
					return false;
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
				if (action.shouldVisitImplicitDestructorNames &&
						!acceptByNodes(((IASTImplicitDestructorNameOwner) expr).getImplicitDestructorNames(), action)) {
					return false;
				}
			}

			if (action.shouldVisitExpressions && action.leave(expr) == ASTVisitor.PROCESS_ABORT)
				return false;

			stack= stack.fNext;
		}

		return true;
	}

	@Override
	public void replace(IASTNode child, IASTNode other) {
		if (child == fOperand1) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			fOperand1 = (ICPPASTExpression) other;
		}
		if (child == fOperand2) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			fOperand2 = (ICPPASTInitializerClause) other;
		}
	}


    @Override
	public ICPPFunction getOverload() {
		ICPPEvaluation eval = getEvaluation();
		if (eval instanceof EvalBinary)
			return ((EvalBinary) eval).getOverload(this);
		return null;
    }

	@Override
	public ICPPEvaluation getEvaluation() {
		if (fEvaluation == null)
			fEvaluation= computeEvaluation();

		return fEvaluation;
	}

	private ICPPEvaluation computeEvaluation() {
		if (fOperand1 == null || fOperand2 == null)
			return EvalFixed.INCOMPLETE;
		
		return new EvalBinary(fOperator, fOperand1.getEvaluation(), fOperand2.getEvaluation(), this);
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
