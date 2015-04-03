/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying masterials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Sergey Prigogin (Google)
 *     Mike Kucera (IBM)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalUnary;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.FunctionSetType;

/**
 * Unary expression in c++
 */
public class CPPASTUnaryExpression extends ASTNode implements ICPPASTUnaryExpression, IASTAmbiguityParent {
	private int fOperator;
    private ICPPASTExpression fOperand;
	private ICPPEvaluation fEvaluation;
    private IASTImplicitName[] fImplicitNames;

    public CPPASTUnaryExpression() {
	}

	public CPPASTUnaryExpression(int operator, IASTExpression operand) {
		fOperator = operator;
		setOperand(operand);
	}

	@Override
	public CPPASTUnaryExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}
	
	@Override
	public CPPASTUnaryExpression copy(CopyStyle style) {
		CPPASTUnaryExpression copy =
				new CPPASTUnaryExpression(fOperator, fOperand == null ? null : fOperand.copy(style));
		return copy(copy, style);
	}

	@Override
	public int getOperator() {
        return fOperator;
    }

    @Override
	public void setOperator(int operator) {
        assertNotFrozen();
        fOperator = operator;
    }

    @Override
	public IASTExpression getOperand() {
        return fOperand;
    }

    @Override
	public void setOperand(IASTExpression expression) {
        assertNotFrozen();
        fOperand = (ICPPASTExpression) expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(OPERAND);
		}
    }

    public boolean isPostfixOperator() {
    	return fOperator == op_postFixDecr || fOperator == op_postFixIncr;
    }

    @Override
	public IASTImplicitName[] getImplicitNames() {
		if (fImplicitNames == null) {
			ICPPFunction overload = getOverload();
			if (overload == null || overload instanceof CPPImplicitFunction) {
				fImplicitNames = IASTImplicitName.EMPTY_NAME_ARRAY;
			} else {
				CPPASTImplicitName operatorName = new CPPASTImplicitName(overload.getNameCharArray(), this);
				operatorName.setOperator(true);
				operatorName.setBinding(overload);
				operatorName.computeOperatorOffsets(fOperand, isPostfixOperator());
				fImplicitNames = new IASTImplicitName[] { operatorName };
			}
		}
		
		return fImplicitNames;
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

        if (fOperand != null && !fOperand.accept(action))
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
        if (child == fOperand) {
            other.setPropertyInParent(child.getPropertyInParent());
            other.setParent(child.getParent());
            fOperand  = (ICPPASTExpression) other;
        }
    }

    @Override
	public ICPPFunction getOverload() {
		ICPPEvaluation eval = getEvaluation();
		if (eval instanceof EvalUnary)
			return ((EvalUnary) eval).getOverload(this);
		return null;
    }
    
	@Override
	public ICPPEvaluation getEvaluation() {
		if (fEvaluation == null) {
			fEvaluation= computeEvaluation();
		}
		return fEvaluation;
	}
	
	private ICPPEvaluation computeEvaluation() {
		if (fOperand == null)
			return EvalFixed.INCOMPLETE;

		final ICPPEvaluation nestedEval = fOperand.getEvaluation();
		if (fOperator == op_bracketedPrimary) 
			return nestedEval;

		if (nestedEval.isFunctionSet() && fOperator == op_amper) {
			return nestedEval;
		} 

		IBinding addressOfQualifiedNameBinding= null;
    	if (fOperator == op_amper && fOperand instanceof IASTIdExpression) {
			IASTName name= ((IASTIdExpression) fOperand).getName();
			if (name instanceof ICPPASTQualifiedName) {
				addressOfQualifiedNameBinding= name.resolveBinding();
				if (addressOfQualifiedNameBinding instanceof IProblemBinding)
					return EvalFixed.INCOMPLETE;
			}
		}
    	return new EvalUnary(fOperator, nestedEval, addressOfQualifiedNameBinding, this);
	}
    
    @Override
	public IType getExpressionType() {
		IType type= getEvaluation().getTypeOrFunctionSet(this);
		if (type instanceof FunctionSetType) {
			type= fOperand.getExpressionType();
			if (fOperator == op_amper) {
				if (fOperand instanceof IASTIdExpression) {
					IASTIdExpression idExpr = (IASTIdExpression) fOperand;
					final IASTName name = idExpr.getName();
					if (name instanceof ICPPASTQualifiedName) {
						IBinding binding = name.resolveBinding();
						if (binding instanceof ICPPMethod) {
							ICPPMethod method = (ICPPMethod) binding;
							if (!method.isStatic()) {
								return new CPPPointerToMemberType(method.getType(), method.getClassOwner(), false, false, false);
							}
						}
					}
				}
				return new CPPPointerType(type);
			}
		}
		return type;
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
