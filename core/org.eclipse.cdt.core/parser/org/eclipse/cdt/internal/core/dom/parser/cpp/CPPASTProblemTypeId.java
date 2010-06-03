/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemTypeId;

/**
 * @author jcamelon
 */
public class CPPASTProblemTypeId extends CPPASTTypeId implements IASTProblemTypeId {
	private IASTProblem problem;
    
    public CPPASTProblemTypeId() {
	}

	public CPPASTProblemTypeId(IASTProblem problem) {
		setProblem(problem);
	}

	@Override
	public CPPASTProblemTypeId copy() {
		IASTProblem problem = getProblem();
		IASTDeclSpecifier declSpec = getDeclSpecifier();
		IASTDeclarator absDecl = getAbstractDeclarator();
		
		CPPASTProblemTypeId copy = new CPPASTProblemTypeId();
		copy.setProblem(problem == null ? null : problem.copy());
		copy.setDeclSpecifier(declSpec == null ? null : declSpec.copy());
		copy.setAbstractDeclarator(absDecl == null ? null : absDecl.copy());
		
		copy.setOffsetAndLength(this);
		return copy;
	}
	
	public IASTProblem getProblem() {
        return problem;
    }
    
    public void setProblem(IASTProblem p) {
        assertNotFrozen();
        problem = p;
        if (p != null) {
			p.setParent(this);
			p.setPropertyInParent(PROBLEM);
		}
    }

    @Override
	public final boolean accept (ASTVisitor action) {
        if (action.shouldVisitProblems) {
		    switch (action.visit(getProblem())) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		    switch (action.leave(getProblem())) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
        return true;
    }
}
