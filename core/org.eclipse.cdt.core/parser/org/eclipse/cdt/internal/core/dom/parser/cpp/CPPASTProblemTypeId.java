/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemTypeId;

/**
 * @author jcamelon
 */
public class CPPASTProblemTypeId extends CPPASTProblemOwner implements IASTProblemTypeId {
	
    public CPPASTProblemTypeId() {
	}

	public CPPASTProblemTypeId(IASTProblem problem) {
		super(problem);
	}

	@Override
	public CPPASTProblemTypeId copy() {
		return copy(CopyStyle.withoutLocations);
	}
	
	@Override
	public CPPASTProblemTypeId copy(CopyStyle style) {
		CPPASTProblemTypeId copy = new CPPASTProblemTypeId();
		return copy(copy, style);
	}

    @Override
	public final boolean accept (ASTVisitor action) {
    	if (action.shouldVisitTypeIds) {
		    switch (action.visit(this)) {
            case ASTVisitor.PROCESS_ABORT: return false;
            case ASTVisitor.PROCESS_SKIP: return true;
            default: break;
        }
		    
		// Visit the problem
		if (!super.accept(action))
			return false;
    	
        if (action.shouldVisitTypeIds && action.leave(this) == ASTVisitor.PROCESS_ABORT) 
        	return false;
    	}
        return true;
    }

	@Override
	public IASTDeclSpecifier getDeclSpecifier() {
		return null;
	}

	@Override
	public void setDeclSpecifier(IASTDeclSpecifier declSpec) {
	}

	@Override
	public IASTDeclarator getAbstractDeclarator() {
		return null;
	}

	@Override
	public void setAbstractDeclarator(IASTDeclarator abstractDeclarator) {
	}
}
