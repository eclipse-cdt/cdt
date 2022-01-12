/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Rational Software - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * @author jcamelon
 */
public class CASTFieldDeclarator extends CASTDeclarator implements IASTFieldDeclarator {
	private IASTExpression bitFieldSize;

	public CASTFieldDeclarator() {
	}

	public CASTFieldDeclarator(IASTName name, IASTExpression bitFieldSize) {
		super(name);
		setBitFieldSize(bitFieldSize);
	}

	@Override
	public CASTFieldDeclarator copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTFieldDeclarator copy(CopyStyle style) {
		CASTFieldDeclarator copy = new CASTFieldDeclarator();
		copy.setBitFieldSize(bitFieldSize == null ? null : bitFieldSize.copy(style));
		return copy(copy, style);
	}

	@Override
	public IASTExpression getBitFieldSize() {
		return bitFieldSize;
	}

	@Override
	public void setBitFieldSize(IASTExpression size) {
		assertNotFrozen();
		bitFieldSize = size;
		if (size != null) {
			size.setParent(this);
			size.setPropertyInParent(FIELD_SIZE);
		}
	}

	@Override
	protected boolean postAccept(ASTVisitor action) {
		if (bitFieldSize != null && !bitFieldSize.accept(action))
			return false;

		return super.postAccept(action);
	}

	@Override
	public void replace(IASTNode child, IASTNode other) {
		if (child == bitFieldSize) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			bitFieldSize = (IASTExpression) other;
		} else {
			super.replace(child, other);
		}
	}
}
