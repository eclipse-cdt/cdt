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
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayDesignator;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CASTArrayDesignator extends CASTNode implements
        ICASTArrayDesignator, IASTAmbiguityParent {

    private IASTExpression exp;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICASTArrayDesignator#getSubscriptExpression()
     */
    public IASTExpression getSubscriptExpression() {
        return exp;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICASTArrayDesignator#setSubscriptExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setSubscriptExpression(IASTExpression value) {
        exp = value;
    }

    public boolean accept( ASTVisitor action ){
        if( action instanceof CASTVisitor && ((CASTVisitor)action).shouldVisitDesignators ){
		    switch( ((CASTVisitor)action).visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
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
