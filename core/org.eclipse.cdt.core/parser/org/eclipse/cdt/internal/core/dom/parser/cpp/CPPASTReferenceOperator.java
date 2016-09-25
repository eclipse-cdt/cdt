/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;

/**
 * Reference operator for declarators.
 */
public class CPPASTReferenceOperator extends CPPASTAttributeOwner implements ICPPASTReferenceOperator {
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
		return copy(copy, style);
	}

    @Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitPointerOperators) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT: return false;
			case ASTVisitor.PROCESS_SKIP: return true;
			}
		}

		if (!acceptByAttributeSpecifiers(action))
			return false;

		if (action.shouldVisitPointerOperators) {
			if (action.leave(this) == ASTVisitor.PROCESS_ABORT)
				return false;
    	}
		return true;
    }
}
