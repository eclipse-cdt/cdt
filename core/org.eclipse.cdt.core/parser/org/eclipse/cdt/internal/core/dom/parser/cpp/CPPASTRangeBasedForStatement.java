/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTRangeBasedForStatement;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * Range based for loop in c++.
 */
public class CPPASTRangeBasedForStatement extends ASTNode implements ICPPASTRangeBasedForStatement, IASTAmbiguityParent {
    private IScope fScope;
    private IASTDeclaration  fDeclaration;
    private IASTInitializerClause fInitClause;
    private IASTStatement fBody;

    public CPPASTRangeBasedForStatement() {
	}

    public CPPASTRangeBasedForStatement copy() {
		CPPASTRangeBasedForStatement copy = new CPPASTRangeBasedForStatement();
		copy.setDeclaration(fDeclaration == null ? null : fDeclaration.copy());
		copy.setInitializerClause(fInitClause == null ? null : fInitClause.copy());
		copy.setBody(fBody == null ? null : fBody.copy());
		copy.setOffsetAndLength(this);
		return copy;
	}
    
	public IASTDeclaration getDeclaration() {
        return fDeclaration;
    }

    public void setDeclaration(IASTDeclaration declaration) {
        assertNotFrozen();
        this.fDeclaration = declaration;
        if (declaration != null) {
        	declaration.setParent(this);
        	declaration.setPropertyInParent(DECLARATION);
		}
    }

    public IASTInitializerClause getInitializerClause() {
        return fInitClause;
    }

    public void setInitializerClause(IASTInitializerClause initClause) {
        assertNotFrozen();
        fInitClause = initClause;
        if (initClause != null) {
			initClause.setParent(this);
			initClause.setPropertyInParent(INITIALIZER);
		}
    }

    public IASTStatement getBody() {
        return fBody;
    }

	public void setBody(IASTStatement statement) {
		assertNotFrozen();
		fBody = statement;
		if (statement != null) {
			statement.setParent(this);
			statement.setPropertyInParent(BODY);
		}
	}

	public IScope getScope() {
		if (fScope == null)
			fScope = new CPPBlockScope(this);
		return fScope;
	}

    @Override
	public boolean accept( ASTVisitor action ){
		if (action.shouldVisitStatements) {
			switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
		if (fDeclaration != null && !fDeclaration.accept(action))
			return false;
		if (fInitClause != null && !fInitClause.accept(action))
			return false;
		if (fBody != null && !fBody.accept(action))
			return false;
        
		if (action.shouldVisitStatements && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;
		return true;
    }
    
	public void replace(IASTNode child, IASTNode other) {
		if (child == fDeclaration) {
			setDeclaration((IASTDeclaration) other);
		} else if (child == fInitClause) {
			setInitializerClause((IASTInitializerClause) other);
		} else if (child == fBody) {
			setBody((IASTStatement) other);
		}
	}
}
