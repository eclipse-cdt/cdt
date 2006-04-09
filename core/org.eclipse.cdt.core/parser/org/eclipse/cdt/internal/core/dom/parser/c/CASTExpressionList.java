/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CASTExpressionList extends CASTNode implements IASTExpressionList,
        IASTAmbiguityParent {

    public IASTExpression[] getExpressions() {
        if (expressions == null)
            return IASTExpression.EMPTY_EXPRESSION_ARRAY;
        return (IASTExpression[]) ArrayUtil.removeNulls( IASTExpression.class, expressions );
    }

    public void addExpression(IASTExpression expression) {
        expressions = (IASTExpression[]) ArrayUtil.append( IASTExpression.class, expressions, expression );
    }

    private IASTExpression [] expressions = new IASTExpression[2];

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

        return true;
    }

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
    
    public IType getExpressionType() {
    	return CVisitor.getExpressionType(this);
    }
    
}
