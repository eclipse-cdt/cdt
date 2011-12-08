/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * Reference operator for declarators.
 */
public class CPPASTReferenceOperator extends ASTNode implements ICPPASTReferenceOperator {
	private final boolean fIsRValue;

	public CPPASTReferenceOperator(boolean isRValueReference) {
		fIsRValue= isRValueReference;
	}
	
	@Override
	public boolean isRValueReference() {
		return fIsRValue;
	}

	@Override
	public CPPASTReferenceOperator copy() {
		return copy(CopyStyle.withoutLocations);
	}
	
	@Override
	public CPPASTReferenceOperator copy(CopyStyle style) {
		CPPASTReferenceOperator copy = new CPPASTReferenceOperator(fIsRValue);
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

    @Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitPointerOperators) {
			switch (action.visit(this)) {
    		case ASTVisitor.PROCESS_ABORT: return false;
    		case ASTVisitor.PROCESS_SKIP: return true;
    		}
			if (action.leave(this) == ASTVisitor.PROCESS_ABORT)
				return false;
    	}
		return true;	    
    }
}
