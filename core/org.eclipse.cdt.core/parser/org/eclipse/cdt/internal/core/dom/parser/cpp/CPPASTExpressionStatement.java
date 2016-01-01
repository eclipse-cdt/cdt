/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * @author jcamelon
 */
public class CPPASTExpressionStatement extends CPPASTAttributeOwner implements IASTExpressionStatement {
    private IASTExpression expression;
    
    public CPPASTExpressionStatement() {
	}

	public CPPASTExpressionStatement(IASTExpression expression) {
		setExpression(expression);
	}

	@Override
	public CPPASTExpressionStatement copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTExpressionStatement copy(CopyStyle style) {
		CPPASTExpressionStatement copy = new CPPASTExpressionStatement();
		copy.setExpression(expression == null ? null : expression.copy(style));
		return copy(copy, style);
	}
	
	@Override
	public IASTExpression getExpression() {
        return expression;
    }

    @Override
	public void setExpression(IASTExpression expression) {
        assertNotFrozen();
        this.expression = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(EXPRESSION);
		}
    }

    @Override
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitStatements) {
		    switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}

        if (!acceptByAttributeSpecifiers(action)) return false;
        if (expression != null && !expression.accept(action)) return false;

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
	public void replace(IASTNode child, IASTNode other) {
        if (child == expression) {
            other.setPropertyInParent(child.getPropertyInParent());
            other.setParent(child.getParent());
            expression = (IASTExpression) other;
            return;
        }
        super.replace(child, other);
    }
}
