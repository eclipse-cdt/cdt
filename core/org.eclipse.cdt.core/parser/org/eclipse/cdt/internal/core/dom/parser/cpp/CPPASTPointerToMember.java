/*******************************************************************************
 * Copyright (c) 2004, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPointerToMember;

public class CPPASTPointerToMember extends CPPASTPointer implements ICPPASTPointerToMember {

	private IASTName n;

	public CPPASTPointerToMember() {
	}

	public CPPASTPointerToMember(IASTName n) {
		setName(n);
	}

	@Override
	public CPPASTPointerToMember copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTPointerToMember copy(CopyStyle style) {
		CPPASTPointerToMember copy = new CPPASTPointerToMember(n == null ? null : n.copy(style));
		copy.setConst(isConst());
		copy.setVolatile(isVolatile());
		copy.setRestrict(isRestrict());
		return copy(copy, style);
	}

	@Override
	public void setName(IASTName name) {
		assertNotFrozen();
		n = name;
		if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(NAME);
		}
	}

	@Override
	public IASTName getName() {
		return n;
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitPointerOperators) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			}
		}
		if (n != null && !n.accept(action))
			return false;

		if (action.shouldVisitPointerOperators && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;
		return true;
	}

	@Override
	public int getRoleForName(IASTName name) {
		if (name == this.n)
			return r_reference;
		return r_unclear;
	}
}
