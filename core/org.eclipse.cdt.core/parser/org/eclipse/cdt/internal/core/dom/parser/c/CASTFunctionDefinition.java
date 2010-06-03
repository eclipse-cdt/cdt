/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Rational Software - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.c.ICFunctionScope;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CASTFunctionDefinition extends ASTNode implements IASTFunctionDefinition, IASTAmbiguityParent {

    private IASTDeclSpecifier declSpecifier;
    private IASTFunctionDeclarator declarator;
    private IASTStatement bodyStatement;
    private ICFunctionScope scope;
    
    public CASTFunctionDefinition() {
	}

	public CASTFunctionDefinition(IASTDeclSpecifier declSpecifier, IASTFunctionDeclarator declarator,
			IASTStatement bodyStatement) {
    	setDeclSpecifier(declSpecifier);
    	setDeclarator(declarator);
    	setBody(bodyStatement);
	}

	public CASTFunctionDefinition copy() {
		CASTFunctionDefinition copy = new CASTFunctionDefinition();
		copy.setDeclSpecifier(declSpecifier == null ? null : declSpecifier.copy());
		
		if (declarator != null) {
			IASTDeclarator outer = CVisitor.findOutermostDeclarator(declarator);
			outer = outer.copy();
			copy.setDeclarator((IASTFunctionDeclarator) CVisitor.findTypeRelevantDeclarator(outer));
		}	
		
		copy.setBody(bodyStatement == null ? null : bodyStatement.copy());
		copy.setOffsetAndLength(this);
		return copy;
	}
	
	public IASTDeclSpecifier getDeclSpecifier() {
        return declSpecifier;
    }

    public void setDeclSpecifier(IASTDeclSpecifier declSpec) {
        assertNotFrozen();
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
        assertNotFrozen();
        this.declarator = declarator;
        if (declarator != null) {
        	IASTDeclarator outerDtor= CVisitor.findOutermostDeclarator(declarator);
        	outerDtor.setParent(this);
        	outerDtor.setPropertyInParent(DECLARATOR);
		}
    }

    public IASTStatement getBody() {
        return bodyStatement;
    }

    public void setBody(IASTStatement statement) {
        assertNotFrozen();
        bodyStatement = statement;
        if (statement != null) {
			statement.setParent(this);
			statement.setPropertyInParent(FUNCTION_BODY);
		}
    }

	public IScope getScope() {
		if (scope == null)
			scope = new CFunctionScope(this);
		return scope;
	}

    @Override
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitDeclarations) {
		    switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
        
        if (declSpecifier != null && !declSpecifier.accept(action)) return false;
        final IASTDeclarator outerDtor= CVisitor.findOutermostDeclarator(declarator);
        if (outerDtor != null && !outerDtor.accept(action)) return false;
        if (bodyStatement != null && !bodyStatement.accept(action)) return false;
      
        if (action.shouldVisitDeclarations) {
		    switch (action.leave(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
        return true;
    }

    public void replace(IASTNode child, IASTNode other) {
        if (bodyStatement == child) {
            other.setPropertyInParent(bodyStatement.getPropertyInParent());
            other.setParent(bodyStatement.getParent());
            bodyStatement = (IASTStatement) other;
        }
    }
}
