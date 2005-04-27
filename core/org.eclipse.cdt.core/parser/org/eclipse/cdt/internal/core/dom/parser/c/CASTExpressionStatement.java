/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CASTExpressionStatement extends CASTNode implements
        IASTExpressionStatement, IASTAmbiguityParent {

    private IASTExpression expression;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTExpressionStatement#getExpression()
     */
    public IASTExpression getExpression() {
        return expression;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTExpressionStatement#setExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setExpression(IASTExpression expression) {
        this.expression = expression;
    }

    public boolean accept(ASTVisitor action) {
        if (action.shouldVisitStatements) {
            switch (action.visit(this)) {
            case ASTVisitor.PROCESS_ABORT:
                return false;
            case ASTVisitor.PROCESS_SKIP:
                return true;
            default:
                break;
            }
        }
        if (expression != null)
            if (!expression.accept(action))
                return false;
        return true;
    }

    public void replace(IASTNode child, IASTNode other) {
        if (child == expression) {
            other.setPropertyInParent(child.getPropertyInParent());
            other.setParent(child.getParent());
            expression = (IASTExpression) other;
        }
    }

}
