/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.internal.core.dom.parser.ASTProblem;

/**
 * C++-specific implementation allows actions to visit the problem.
 */
public class CPPASTProblem extends ASTProblem {
   
    public CPPASTProblem(int id, char[] arg, boolean isError) {
    	super(id, arg, isError);
    }

    @Override
	public CPPASTProblem copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTProblem copy(CopyStyle style) {
    	char[] arg = getArgument();
    	CPPASTProblem problem = new CPPASTProblem(getID(), arg == null ? null : arg.clone(), isError());
		problem.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			problem.setCopyLocation(this);
		}
		return problem;
	}
    
    @Override
	public boolean accept(ASTVisitor action) {
    	if (action.shouldVisitProblems) {
		    switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
    	if (action.shouldVisitProblems) {
		    switch (action.leave(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
        return true;
    }
}
