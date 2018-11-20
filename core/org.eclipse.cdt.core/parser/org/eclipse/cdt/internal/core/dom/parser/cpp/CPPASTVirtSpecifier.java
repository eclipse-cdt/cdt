/*******************************************************************************
 * Copyright (c) 2014 Nathan Ridge.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nathan Ridge - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVirtSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

public class CPPASTVirtSpecifier extends ASTNode implements ICPPASTVirtSpecifier {
	private SpecifierKind fKind;

	public CPPASTVirtSpecifier(SpecifierKind kind) {
		fKind = kind;
	}

	@Override
	public SpecifierKind getKind() {
		return fKind;
	}

	@Override
	public ICPPASTVirtSpecifier copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public ICPPASTVirtSpecifier copy(CopyStyle style) {
		CPPASTVirtSpecifier copy = new CPPASTVirtSpecifier(fKind);
		return copy(copy, style);
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitVirtSpecifiers) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}

		if (action.shouldVisitVirtSpecifiers) {
			switch (action.leave(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}
		return true;
	}
}
