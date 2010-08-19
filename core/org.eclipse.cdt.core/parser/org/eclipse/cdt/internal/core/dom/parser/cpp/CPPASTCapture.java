/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCapture;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * Implementation for captures.
 */
public class CPPASTCapture extends ASTNode implements ICPPASTCapture {
	private boolean fByReference;
	private boolean fPackExpansion;
	private IASTName fIdentifier;

	public CPPASTCapture() {
	}

	public CPPASTCapture copy() {
		final CPPASTCapture result= new CPPASTCapture();
		if (fIdentifier != null)
			result.setIdentifier(fIdentifier.copy());
		result.fByReference= fByReference;
		result.fPackExpansion= fPackExpansion;
		result.setOffsetAndLength(this);
		return result;
	}

	public boolean capturesThisPointer() {
		return fIdentifier == null;
	}

	public boolean isByReference() {
		return fByReference;
	}

	public boolean isPackExpansion() {
		return fPackExpansion;
	}

	public IASTName getIdentifier() {
		return fIdentifier;
	}

	@Override
	public boolean accept(ASTVisitor visitor) {
        if (visitor.shouldVisitCaptures) {
		    switch (visitor.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}

		if (fIdentifier != null && !fIdentifier.accept(visitor))
			return false;

		if (visitor.shouldVisitCaptures && visitor.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;

        return true;
    }

	public void setIdentifier(IASTName identifier) {
		assertNotFrozen();
		if (identifier != null) {
			identifier.setParent(this);
			identifier.setPropertyInParent(IDENTIFIER);
		}
		fIdentifier= identifier;
	}

	public void setIsByReference(boolean value) {
		assertNotFrozen();
		fByReference= value;
	}

	public void setIsPackExpansion(boolean val) {
		assertNotFrozen();
		fPackExpansion= val;
	}
}
