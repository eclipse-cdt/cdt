/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;

/**
 * @author jcamelon
 */
public class CPPASTFunctionCallExpression extends CPPASTNode implements
        IASTFunctionCallExpression {
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
}
