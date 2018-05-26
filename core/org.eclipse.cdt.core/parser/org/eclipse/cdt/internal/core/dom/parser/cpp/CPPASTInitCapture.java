/*******************************************************************************
 * Copyright (c) 2018 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Hansruedi Patzen (IFS) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCapture;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitCapture;

/**
 * Implementation for init captures.
 */
public class CPPASTInitCapture extends CPPASTCapture implements ICPPASTInitCapture {
	private IASTDeclarator fDeclarator;

	public CPPASTInitCapture(IASTDeclarator declarator) {
		setDeclarator(declarator);
	}

	@Override
	public CPPASTInitCapture copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTInitCapture copy(CopyStyle style) {
		final CPPASTInitCapture copy = new CPPASTInitCapture(fDeclarator != null ? fDeclarator.copy(style) : null);
		copy.setIsByReference(isByReference());
		copy.setIsPackExpansion(isPackExpansion());
		return copy(copy, style);
	}

	@Override
	public boolean accept(ASTVisitor visitor) {
		if (visitor.shouldVisitCaptures) {
			switch (visitor.visit((ICPPASTCapture) this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}

		if (visitor.shouldVisitDeclarations) {
			switch (visitor.visit((IASTDeclaration) this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			}
		}

		if (fDeclarator != null && !fDeclarator.accept(visitor)) {
			return false;
		}

		if (visitor.shouldVisitCaptures && visitor.leave((ICPPASTCapture) this) == ASTVisitor.PROCESS_ABORT) {
			return false;
		}

		if (visitor.shouldVisitDeclarations && visitor.leave((IASTDeclaration) this) == ASTVisitor.PROCESS_ABORT) {
			return false;
		}

		return true;
	}

	@Override
	public boolean capturesThisPointer() {
		return false;
	}

	@Override
	public int getRoleForName(IASTName name) {
		if (fDeclarator != null && name == fDeclarator.getName()) {
			return r_declaration;
		}
		return r_unclear;
	}

	@Override
	public IASTDeclarator getDeclarator() {
		return fDeclarator;
	}

	@Override
	public void setDeclarator(IASTDeclarator declarator) {
		assertNotFrozen();
		fDeclarator = declarator;
		fDeclarator.setParent(this);
	}

	@Override
	public IASTName getIdentifier() {
		return fDeclarator.getName();
	}

	@Override
	public void setIdentifier(IASTName identifier) {
		throw new UnsupportedOperationException();
	}
}
