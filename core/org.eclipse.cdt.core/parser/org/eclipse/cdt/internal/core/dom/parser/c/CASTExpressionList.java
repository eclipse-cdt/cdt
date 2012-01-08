/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Yuan Zhang / Beth Tibbitts (IBM Research)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;

/**
 * Expression list in C
 */
public class CASTExpressionList extends ASTNode implements IASTExpressionList,
        IASTAmbiguityParent {

	@Override
	public CASTExpressionList copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTExpressionList copy(CopyStyle style) {
		CASTExpressionList copy = new CASTExpressionList();
		for(IASTExpression expr : getExpressions())
			copy.addExpression(expr == null ? null : expr.copy(style));
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}
	
    @Override
	public IASTExpression[] getExpressions() {
        if (expressions == null)
            return IASTExpression.EMPTY_EXPRESSION_ARRAY;
        return ArrayUtil.trim( IASTExpression.class, expressions );
    }

    @Override
	public void addExpression(IASTExpression expression) {
        assertNotFrozen();
        expressions = ArrayUtil.append( IASTExpression.class, expressions, expression );
        if(expression != null) {
        	expression.setParent(this);
    		expression.setPropertyInParent(NESTED_EXPRESSION);
		}
    }

    private IASTExpression [] expressions = new IASTExpression[2];

    @Override
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitExpressions) {
            switch (action.visit(this)) {
            case ASTVisitor.PROCESS_ABORT:
                return false;
            case ASTVisitor.PROCESS_SKIP:
                return true;
            default:
                break;
            }
        }

        IASTExpression[] exps = getExpressions();
        for (int i = 0; i < exps.length; i++)
            if (!exps[i].accept(action))
                return false;

        if (action.shouldVisitExpressions) {
            switch (action.leave(this)) {
            case ASTVisitor.PROCESS_ABORT:
                return false;
            case ASTVisitor.PROCESS_SKIP:
                return true;
            default:
                break;
            }
        }
        return true;
    }

    @Override
	public void replace(IASTNode child, IASTNode other) {
        if( expressions == null ) return;
        for (int i = 0; i < expressions.length; ++i) {
            if (child == expressions[i]) {
                other.setPropertyInParent(child.getPropertyInParent());
                other.setParent(child.getParent());
                expressions[i] = (IASTExpression) other;
            }
        }
    }
    
    @Override
	public IType getExpressionType() {
    	for (int i = expressions.length-1; i >= 0; i--) {
    		IASTExpression expr= expressions[i];
    		if (expr != null)
    			return expr.getExpressionType();
		}
    	return new ProblemType(ISemanticProblem.TYPE_UNKNOWN_FOR_EXPRESSION);
    }

	@Override
	public boolean isLValue() {
    	for (int i = expressions.length-1; i >= 0; i--) {
    		IASTExpression expr= expressions[i];
    		if (expr != null)
    			return expr.isLValue();
		}
    	return false;
	}
	
	@Override
	public final ValueCategory getValueCategory() {
		return isLValue() ? ValueCategory.LVALUE : ValueCategory.PRVALUE;
	}
}
