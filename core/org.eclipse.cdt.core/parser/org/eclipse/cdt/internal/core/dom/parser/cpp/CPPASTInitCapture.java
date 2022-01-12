/*******************************************************************************
 * Copyright (c) 2018 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Hansruedi Patzen (IFS) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.Objects;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCapture;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitCapture;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;

/**
 * Implementation for init captures.
 */
public class CPPASTInitCapture extends CPPASTCaptureBase implements ICPPASTInitCapture {
	private ICPPASTDeclarator fDeclarator;

	public CPPASTInitCapture(ICPPASTDeclarator declarator) {
		setDeclarator(declarator);
	}

	@Override
	public CPPASTInitCapture copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTInitCapture copy(CopyStyle style) {
		final CPPASTInitCapture copy = new CPPASTInitCapture((ICPPASTDeclarator) fDeclarator.copy(style));
		copy.setIsByReference(false);
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

		if (visitor.shouldVisitDeclarations && visitor.leave((IASTDeclaration) this) == ASTVisitor.PROCESS_ABORT) {
			return false;
		}

		if (visitor.shouldVisitCaptures && visitor.leave((ICPPASTCapture) this) == ASTVisitor.PROCESS_ABORT) {
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
		if (name == fDeclarator.getName()) {
			return r_declaration;
		}
		return r_unclear;
	}

	@Override
	public boolean isByReference() {
		IASTPointerOperator[] pointerOperators = fDeclarator.getPointerOperators();
		return pointerOperators.length == 1 && pointerOperators[0] instanceof ICPPASTReferenceOperator;
	}

	@Override
	public void setIsByReference(boolean value) {
		assertNotFrozen();
		boolean isReferenceCapture = isByReference();
		if (value && !isReferenceCapture) {
			fDeclarator.addPointerOperator(CPPNodeFactory.getDefault().newReferenceOperator(false));
		} else if (!value && isReferenceCapture) {
			// Use non API removePointerOperator
			((CPPASTDeclarator) fDeclarator).removePointerOperator(fDeclarator.getPointerOperators()[0]);
		}
	}

	@Override
	public ICPPASTDeclarator getDeclarator() {
		return fDeclarator;
	}

	@Override
	public void setDeclarator(ICPPASTDeclarator declarator) {
		assertNotFrozen();
		Objects.requireNonNull(declarator, "An init capture declarator must not be null."); //$NON-NLS-1$
		fDeclarator = declarator;
		fDeclarator.setParent(this);
		fDeclarator.setPropertyInParent(DECLARATOR);
	}

	@Override
	public IASTName getIdentifier() {
		return fDeclarator.getName();
	}

	@Override
	public void setIdentifier(IASTName identifier) {
		assertNotFrozen();
		Objects.requireNonNull(identifier, "An init capture must have an identifier."); //$NON-NLS-1$
		fDeclarator.setName(identifier);
	}
}
