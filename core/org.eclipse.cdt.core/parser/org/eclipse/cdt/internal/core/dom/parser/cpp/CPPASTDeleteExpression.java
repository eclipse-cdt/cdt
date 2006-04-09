/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;

/**
 * @author jcamelon
 */
public class CPPASTDeleteExpression extends CPPASTNode implements
        ICPPASTDeleteExpression {

    private IASTExpression operand;
    private boolean isGlobal;
    private boolean isVectored;

    public IASTExpression getOperand() {
        return operand;
    }

    public void setOperand(IASTExpression expression) {
        operand = expression;
    }

    public void setIsGlobal(boolean global) {
        isGlobal = global;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public void setIsVectored(boolean vectored) {
        isVectored = vectored;
    }

    public boolean isVectored() {
        return isVectored;
    }

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitExpressions ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        
        if( operand != null ) if( !operand.accept( action ) ) return false;
        return true;
    }
    
    public IType getExpressionType() {
    	return CPPVisitor.getExpressionType(this);
    }
    
}
