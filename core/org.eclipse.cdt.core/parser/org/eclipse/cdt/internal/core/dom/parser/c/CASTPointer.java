/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.c.ICASTPointer;
import org.eclipse.cdt.internal.core.dom.parser.ASTAttributeOwner;

public class CASTPointer extends ASTAttributeOwner implements ICASTPointer {

	private boolean isRestrict;
	private boolean isVolatile;
	private boolean isConst;

	@Override
	public CASTPointer copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTPointer copy(CopyStyle style) {
		CASTPointer copy = new CASTPointer();
		copy.isRestrict = isRestrict;
		copy.isVolatile = isVolatile;
		copy.isConst = isConst;
		return copy(copy, style);
	}

	@Override
	public boolean isRestrict() {
		return isRestrict;
	}

	@Override
	public void setRestrict(boolean value) {
		assertNotFrozen();
		isRestrict = value;
	}

	@Override
	public boolean isConst() {
		return isConst;
	}

	@Override
	public boolean isVolatile() {
		return isVolatile;
	}

	@Override
	public void setConst(boolean value) {
		assertNotFrozen();
		isConst = value;
	}

	@Override
	public void setVolatile(boolean value) {
		assertNotFrozen();
		isVolatile = value;
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

		if (!acceptByAttributeSpecifiers(action))
			return false;

		if (action.shouldVisitPointerOperators) {
			if (action.leave(this) == ASTVisitor.PROCESS_ABORT)
				return false;
		}
		return true;
	}
}
