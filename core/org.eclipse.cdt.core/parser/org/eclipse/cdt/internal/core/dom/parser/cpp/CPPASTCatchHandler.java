/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CPPASTCatchHandler extends ASTNode implements ICPPASTCatchHandler, IASTAmbiguityParent {
    private boolean isCatchAll;
    private IASTStatement body;
    private IASTDeclaration declaration;
	private IScope scope;
    
    public CPPASTCatchHandler() {
	}

	public CPPASTCatchHandler(IASTDeclaration declaration, IASTStatement body) {
		setCatchBody(body);
		setDeclaration(declaration);
	}
	
	@Override
	public CPPASTCatchHandler copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTCatchHandler copy(CopyStyle style) {
		CPPASTCatchHandler copy = new CPPASTCatchHandler();
		copy.setDeclaration(declaration == null ? null : declaration.copy(style));
		copy.setCatchBody(body == null ? null : body.copy(style));
		copy.setIsCatchAll(isCatchAll);
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

	@Override
	public void setIsCatchAll(boolean isEllipsis) {
        assertNotFrozen();
        isCatchAll = isEllipsis;
    }

    @Override
	public boolean isCatchAll() {
        return isCatchAll;
    }

    @Override
	public void setCatchBody(IASTStatement compoundStatement) {
        assertNotFrozen();
        body = compoundStatement;
        if (compoundStatement != null) {
			compoundStatement.setParent(this);
			compoundStatement.setPropertyInParent(CATCH_BODY);
		}
    }

    @Override
	public IASTStatement getCatchBody() {
        return body;
    }

    @Override
	public void setDeclaration(IASTDeclaration decl) {
        assertNotFrozen();
        declaration = decl;
        if (decl != null) {
			decl.setParent(this);
			decl.setPropertyInParent(DECLARATION);
		}
    }

    @Override
	public IASTDeclaration getDeclaration() {
        return declaration;
    }

    @Override
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitStatements) {
		    switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
        if (declaration != null && !declaration.accept(action)) return false;
        if (body != null && !body.accept(action)) return false;
        
        if (action.shouldVisitStatements) {
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
        if (body == child) {
            other.setPropertyInParent(child.getPropertyInParent());
            other.setParent(child.getParent());
            body = (IASTStatement) other;
        }
        if (declaration == child) {
            other.setParent(child.getParent());
            other.setPropertyInParent(child.getPropertyInParent());
            declaration = (IASTDeclaration) other;
        }
    }

	@Override
	public IScope getScope() {
		if (scope == null) {
			scope = new CPPBlockScope(this);
		}
		return scope;
	}
}
