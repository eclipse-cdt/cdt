/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation */
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CASTFunctionCallExpression extends CASTNode implements
        IASTFunctionCallExpression, IASTAmbiguityParent {

    private IASTExpression functionName;
    private IASTExpression parameter;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression#setFunctionNameExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setFunctionNameExpression(IASTExpression expression) {
        this.functionName = expression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression#getFunctionNameExpression()
     */
    public IASTExpression getFunctionNameExpression() {
        return functionName;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression#setParameterExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setParameterExpression(IASTExpression expression) {
        this.parameter = expression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression#getParameterExpression()
     */
    public IASTExpression getParameterExpression() {
        return parameter;
    }

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitExpressions ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
      
        if( functionName != null ) if( !functionName.accept( action ) ) return false;
        if( parameter != null )  if( !parameter.accept( action ) ) return false;
        return true;
    }

    public void replace(IASTNode child, IASTNode other) {
        if( child == functionName )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            functionName  = (IASTExpression) other;
        }
        if( child == parameter)
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            parameter = (IASTExpression) other;
        }
    }

}
