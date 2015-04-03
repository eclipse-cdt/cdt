/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.internal.core.dom.parser.ASTAttributeOwner;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.DestructorCallCollector;

/**
 * @author jcamelon
 */
public class CPPASTCatchHandler extends ASTAttributeOwner
		implements ICPPASTCatchHandler, IASTAmbiguityParent {
    private boolean fIsCatchAll;
    private IASTStatement fBody;
    private IASTDeclaration fDeclaration;
	private IScope fScope;
	private IASTImplicitDestructorName[] fImplicitDestructorNames;
    
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
		copy.setDeclaration(fDeclaration == null ? null : fDeclaration.copy(style));
		copy.setCatchBody(fBody == null ? null : fBody.copy(style));
		copy.setIsCatchAll(fIsCatchAll);
		return copy(copy, style);
	}

	@Override
	public void setIsCatchAll(boolean isEllipsis) {
        assertNotFrozen();
        fIsCatchAll = isEllipsis;
    }

    @Override
	public boolean isCatchAll() {
        return fIsCatchAll;
    }

    @Override
	public void setCatchBody(IASTStatement compoundStatement) {
        assertNotFrozen();
        fBody = compoundStatement;
        if (compoundStatement != null) {
			compoundStatement.setParent(this);
			compoundStatement.setPropertyInParent(CATCH_BODY);
		}
    }

    @Override
	public IASTStatement getCatchBody() {
        return fBody;
    }

    @Override
	public void setDeclaration(IASTDeclaration decl) {
        assertNotFrozen();
        fDeclaration = decl;
        if (decl != null) {
			decl.setParent(this);
			decl.setPropertyInParent(DECLARATION);
		}
    }

    @Override
	public IASTDeclaration getDeclaration() {
        return fDeclaration;
    }

	@Override
	public IASTImplicitDestructorName[] getImplicitDestructorNames() {
		if (fImplicitDestructorNames == null) {
			fImplicitDestructorNames = DestructorCallCollector.getLocalVariablesDestructorCalls(this);
		}

		return fImplicitDestructorNames;
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

        if (!acceptByAttributeSpecifiers(action)) return false;
        if (fDeclaration != null && !fDeclaration.accept(action)) return false;
        if (fBody != null && !fBody.accept(action)) return false;

        if (action.shouldVisitImplicitDestructorNames && !acceptByNodes(getImplicitDestructorNames(), action))
        	return false;

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
        if (fBody == child) {
            other.setPropertyInParent(child.getPropertyInParent());
            other.setParent(child.getParent());
            fBody = (IASTStatement) other;
        }
        if (fDeclaration == child) {
            other.setParent(child.getParent());
            other.setPropertyInParent(child.getPropertyInParent());
            fDeclaration = (IASTDeclaration) other;
        }
    }

	@Override
	public IScope getScope() {
		if (fScope == null) {
			fScope = new CPPBlockScope(this);
		}
		return fScope;
	}
}
