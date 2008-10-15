/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * @author jcamelon
 */
public class CPPASTCompoundStatementExpression extends ASTNode implements IGNUASTCompoundStatementExpression {
	
    private IASTCompoundStatement statement;

    
    public CPPASTCompoundStatementExpression() {
	}

	public CPPASTCompoundStatementExpression(IASTCompoundStatement statement) {
		setCompoundStatement(statement);
	}

	public IASTCompoundStatement getCompoundStatement() {
        return statement;
    }

    public void setCompoundStatement(IASTCompoundStatement statement) {
        this.statement = statement;
        if (statement != null) {
			statement.setParent(this);
			statement.setPropertyInParent(STATEMENT);
		}
    }

    @Override
	public boolean accept( ASTVisitor action ){
        if( action.shouldVisitExpressions ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        
        if( statement != null ) if( !statement.accept( action ) ) return false;
        
        if( action.shouldVisitExpressions ){
        	switch( action.leave( this ) ){
        		case ASTVisitor.PROCESS_ABORT : return false;
        		case ASTVisitor.PROCESS_SKIP  : return true;
        		default : break;
        	}
        }
        return true;
    }
    
    public IType getExpressionType() {
    	return CPPVisitor.getExpressionType(this);
    }
    
}
