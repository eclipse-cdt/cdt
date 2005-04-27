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
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CASTArrayModifier extends CASTNode implements IASTArrayModifier, IASTAmbiguityParent {

    private IASTExpression exp;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTArrayModifier#getConstantExpression()
     */
    public IASTExpression getConstantExpression() {
        return exp;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTArrayModifier#setConstantExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setConstantExpression(IASTExpression expression) {
        this.exp = expression;
    }

    public boolean accept( ASTVisitor action ){      
        if( exp != null ) if( !exp.accept( action ) ) return false;
        return true;
    }

    public void replace(IASTNode child, IASTNode other) {
        if( child == exp )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            exp = (IASTExpression) other;
        }
    }
}
