/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;

/**
 * @author jcamelon
 */
public class CPPASTProblemDeclaration extends CPPASTProblemOwner implements IASTProblemDeclaration {

    public CPPASTProblemDeclaration() {
		super();
	}

	public CPPASTProblemDeclaration(IASTProblem problem) {
		super(problem);
	}

	@Override
	public CPPASTProblemDeclaration copy() {
		return copy(CopyStyle.withoutLocations);
	}
	
	@Override
	public CPPASTProblemDeclaration copy(CopyStyle style) {
		CPPASTProblemDeclaration copy = new CPPASTProblemDeclaration();
		copyBaseProblem(copy, style);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}
	
	@Override
	public boolean accept( ASTVisitor action ){
        if( action.shouldVisitDeclarations ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        super.accept(action);		// visits the problem
        if( action.shouldVisitDeclarations ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }

}
