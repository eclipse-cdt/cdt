/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Mike Kucera (IBM) - implicit names
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpressionList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.DestructorCallCollector;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalComma;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;

public class CPPASTExpressionList extends ASTNode implements ICPPASTExpressionList, IASTAmbiguityParent {
    private IASTExpression[] expressions = new IASTExpression[2];
    
	/**
	 * Caution: may contain nulls. 
	 * @see #computeImplicitNames
	 */
	private IASTImplicitName[] fImplicitNames;

	private IASTImplicitDestructorName[] fImplicitDestructorNames;

	private ICPPEvaluation fEvaluation;

	@Override
	public CPPASTExpressionList copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTExpressionList copy(CopyStyle style) {
		CPPASTExpressionList copy = new CPPASTExpressionList();
		for (IASTExpression expr : getExpressions()) {
			copy.addExpression(expr == null ? null : expr.copy(style));
		}
		return copy(copy, style);
	}
	
	@Override
	public IASTExpression[] getExpressions() {
        if (expressions == null) return IASTExpression.EMPTY_EXPRESSION_ARRAY;
        return ArrayUtil.trim(IASTExpression.class, expressions);
    }

    @Override
	public void addExpression(IASTExpression expression) {
        assertNotFrozen();
		expressions = ArrayUtil.append(expressions, expression);
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(NESTED_EXPRESSION);
		}
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
      
        IASTExpression[] exps = getExpressions();
        IASTImplicitName[] implicits = action.shouldVisitImplicitNames ? computeImplicitNames() : null;

        for (int i = 0, n = exps.length; i < n; i++) {
        	if (!exps[i].accept(action)) { 
        		return false;
        	}
        	if (i < n - 1 && implicits != null && implicits[i] != null) {
        		if (!implicits[i].accept(action)) {
        			return false;
        		}
        	}
        }

        if (action.shouldVisitImplicitDestructorNames && !acceptByNodes(getImplicitDestructorNames(), action))
        	return false;
        
        if (action.shouldVisitExpressions) {
		    switch (action.leave(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		} 
        return true;
    }

    /**
     * Returns an array of implicit names where each element of the array represents a comma between
     * the expression in the same index and the next expression. This array contains null elements
     * as placeholders for commas that do not resolve to overloaded operators.
     */
    private IASTImplicitName[] computeImplicitNames() {
		if (fImplicitNames == null) {
			IASTExpression[] exprs = getExpressions(); // has to be at least two
			if (exprs.length < 2)
				return fImplicitNames = IASTImplicitName.EMPTY_NAME_ARRAY;
			
			fImplicitNames = new IASTImplicitName[exprs.length - 1];
			
			ICPPFunction[] overloads = getOverloads();
			for (int i = 0; i < overloads.length; i++) {
				ICPPFunction overload = overloads[i];
				if (overload != null && !(overload instanceof CPPImplicitFunction)) {
					CPPASTImplicitName operatorName = new CPPASTImplicitName(OverloadableOperator.COMMA, this);
					operatorName.setBinding(overload);
					operatorName.computeOperatorOffsets(exprs[i], true);
					fImplicitNames[i] = operatorName;
				}
			}
		}
		
		return fImplicitNames;
	}

    @Override
	public IASTImplicitName[] getImplicitNames() {
    	return ArrayUtil.removeNulls(IASTImplicitName.class, computeImplicitNames());
    }

	@Override
	public IASTImplicitDestructorName[] getImplicitDestructorNames() {
		if (fImplicitDestructorNames == null) {
			fImplicitDestructorNames = DestructorCallCollector.getTemporariesDestructorCalls(this);
		}

		return fImplicitDestructorNames;
	}

    private ICPPFunction[] getOverloads() {
    	ICPPEvaluation eval = getEvaluation();
    	if (eval instanceof EvalComma) {
    		return ((EvalComma) eval).getOverloads(this);
    	}
    	return null;
    }

    @Override
	public void replace(IASTNode child, IASTNode other) {
        if (expressions == null) return;
        for (int i = 0; i < expressions.length; ++i) {
            if (child == expressions[i]) {
                other.setPropertyInParent(child.getPropertyInParent());
                other.setParent(child.getParent());
                expressions[i] = (IASTExpression) other;
            }
        }
    }
    
	@Override
	public ICPPEvaluation getEvaluation() {
		if (fEvaluation == null) 
			fEvaluation= computeEvaluation();
		
		return fEvaluation;
	}
	
	private ICPPEvaluation computeEvaluation() {
		final IASTExpression[] exprs = getExpressions();
		if (exprs.length < 2)
			return EvalFixed.INCOMPLETE;
		
		ICPPEvaluation[] evals= new ICPPEvaluation[exprs.length];
		for (int i = 0; i < evals.length; i++) {
			evals[i]= ((ICPPASTExpression) exprs[i]).getEvaluation();
		}
		return new EvalComma(evals, this);
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
