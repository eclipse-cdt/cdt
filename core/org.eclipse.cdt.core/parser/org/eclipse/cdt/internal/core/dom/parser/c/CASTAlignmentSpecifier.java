/*******************************************************************************
 * Copyright (c) 2015 Nathan Ridge.
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
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTAlignmentSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

public class CASTAlignmentSpecifier extends ASTNode implements IASTAlignmentSpecifier {
	// Precisely one of these is null.
	private IASTExpression fExpression;
	private IASTTypeId fTypeId;

	CASTAlignmentSpecifier(IASTExpression expression) {
		fExpression = expression;
		fExpression.setParent(this);
		fExpression.setPropertyInParent(ALIGNMENT_EXPRESSION);
	}

	CASTAlignmentSpecifier(IASTTypeId typeId) {
		fTypeId = typeId;
		fTypeId.setParent(this);
		fTypeId.setPropertyInParent(ALIGNMENT_TYPEID);
	}

	@Override
	public IASTExpression getExpression() {
		return fExpression;
	}

	@Override
	public IASTTypeId getTypeId() {
		return fTypeId;
	}

	@Override
	public IASTAlignmentSpecifier copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public IASTAlignmentSpecifier copy(CopyStyle style) {
		CASTAlignmentSpecifier copy;
		if (fExpression != null) {
			copy = new CASTAlignmentSpecifier(fExpression.copy(style));
		} else {
			copy = new CASTAlignmentSpecifier(fTypeId.copy(style));
		}
		return copy(copy, style);
	}

	@Override
	public boolean accept(ASTVisitor visitor) {
		if (fExpression != null) {
			return fExpression.accept(visitor);
		}
		return fTypeId.accept(visitor);
	}
}
