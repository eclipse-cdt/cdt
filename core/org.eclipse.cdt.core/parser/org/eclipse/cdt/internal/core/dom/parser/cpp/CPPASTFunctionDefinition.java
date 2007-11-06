/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CPPASTFunctionDefinition extends CPPASTNode implements
        IASTFunctionDefinition, IASTAmbiguityParent {

    private IASTDeclSpecifier declSpecifier;
    private IASTFunctionDeclarator declarator;
    private IASTStatement bodyStatement;

    public CPPASTFunctionDefinition() {
	}

	public CPPASTFunctionDefinition(IASTDeclSpecifier declSpecifier,
			IASTFunctionDeclarator declarator, IASTStatement bodyStatement) {
		setDeclSpecifier(declSpecifier);
		setDeclarator(declarator);
		setBody(bodyStatement);
	}

	public IASTDeclSpecifier getDeclSpecifier() {
        return declSpecifier;
    }

    public void setDeclSpecifier(IASTDeclSpecifier declSpec) {
        declSpecifier = declSpec;
        if (declSpec != null) {
			declSpec.setParent(this);
			declSpec.setPropertyInParent(DECL_SPECIFIER);
		}
    }

    public IASTFunctionDeclarator getDeclarator() {
        return declarator;
    }

    public void setDeclarator(IASTFunctionDeclarator declarator) {
        this.declarator = declarator;
        if (declarator != null) {
			declarator.setParent(this);
			declarator.setPropertyInParent(DECLARATOR);
		}
    }

    public IASTStatement getBody() {
        return bodyStatement;
    }

    public void setBody(IASTStatement statement) {
        bodyStatement = statement;
        if (statement != null) {
			statement.setParent(this);
			statement.setPropertyInParent(FUNCTION_BODY);
		} 
    }

	public IScope getScope() {
		return ((ICPPASTFunctionDeclarator)declarator).getFunctionScope();
	}

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitDeclarations ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        
        if( declSpecifier != null ) if( !declSpecifier.accept( action ) ) return false;
        if( declarator != null ) if( !declarator.accept( action ) ) return false;
        if( bodyStatement != null ) if( !bodyStatement.accept( action ) ) return false;
        
        if( action.shouldVisitDeclarations ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }

    public void replace(IASTNode child, IASTNode other) {
        if( bodyStatement == child ) 
        {
            other.setPropertyInParent( bodyStatement.getPropertyInParent() );
            other.setParent( bodyStatement.getParent() );
            bodyStatement = (IASTStatement) other;
        }
    }
    
}
