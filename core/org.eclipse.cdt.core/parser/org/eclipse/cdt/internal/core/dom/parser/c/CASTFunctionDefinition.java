/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
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
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
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

	@Override
	public CASTFunctionDefinition copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTFunctionDefinition copy(CopyStyle style) {
		CASTFunctionDefinition copy = new CASTFunctionDefinition();
		copy.setDeclSpecifier(declSpecifier == null ? null : declSpecifier.copy(style));
		
		if (declarator != null) {
			IASTDeclarator outer = ASTQueries.findOutermostDeclarator(declarator);
			outer = outer.copy(style);
			copy.setDeclarator((IASTFunctionDeclarator) ASTQueries.findTypeRelevantDeclarator(outer));
		}	
		
		copy.setBody(bodyStatement == null ? null : bodyStatement.copy(style));
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}

		return copy;
	}
	
	@Override
	public IASTDeclSpecifier getDeclSpecifier() {
        return declSpecifier;
    }

    @Override
	public void setDeclSpecifier(IASTDeclSpecifier declSpec) {
        assertNotFrozen();
        declSpecifier = declSpec;
        if (declSpec != null) {
			declSpec.setParent(this);
			declSpec.setPropertyInParent(DECL_SPECIFIER);
		}
    }

    @Override
	public IASTFunctionDeclarator getDeclarator() {
        return declarator;
    }

    @Override
	public void setDeclarator(IASTFunctionDeclarator declarator) {
        assertNotFrozen();
        this.declarator = declarator;
        if (declarator != null) {
        	IASTDeclarator outerDtor= ASTQueries.findOutermostDeclarator(declarator);
        	outerDtor.setParent(this);
        	outerDtor.setPropertyInParent(DECLARATOR);
		}
    }

    @Override
	public IASTStatement getBody() {
        return bodyStatement;
    }

    @Override
	public void setBody(IASTStatement statement) {
        assertNotFrozen();
        bodyStatement = statement;
        if (statement != null) {
			statement.setParent(this);
			statement.setPropertyInParent(FUNCTION_BODY);
		}
    }

	@Override
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
        final IASTDeclarator outerDtor= ASTQueries.findOutermostDeclarator(declarator);
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

    @Override
	public void replace(IASTNode child, IASTNode other) {
        if (bodyStatement == child) {
            other.setPropertyInParent(bodyStatement.getPropertyInParent());
            other.setParent(bodyStatement.getParent());
            bodyStatement = (IASTStatement) other;
        }
    }
}
