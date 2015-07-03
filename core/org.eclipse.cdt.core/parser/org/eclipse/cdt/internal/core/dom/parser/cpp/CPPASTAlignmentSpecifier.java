/*******************************************************************************
 * Copyright (c) 2015 Nathan Ridge.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Nathan Ridge - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTAlignmentSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

public class CPPASTAlignmentSpecifier extends ASTNode implements IASTAlignmentSpecifier,
		IASTAmbiguityParent {
	// Precisely one of these is null.
	private IASTExpression fExpression;
	private IASTTypeId fTypeId;
	
	CPPASTAlignmentSpecifier(IASTExpression expression) {
		fExpression = expression;
		fExpression.setParent(this);
		fExpression.setPropertyInParent(ALIGNMENT_EXPRESSION);
	}
	
	CPPASTAlignmentSpecifier(IASTTypeId typeId) {
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
		CPPASTAlignmentSpecifier copy;
		if (fExpression != null) {
			copy = new CPPASTAlignmentSpecifier(fExpression.copy(style));
		} else {
			copy = new CPPASTAlignmentSpecifier(fTypeId.copy(style));
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

	@Override
	public void replace(IASTNode child, IASTNode other) {
		if (child instanceof IASTExpression && other instanceof IASTExpression && fExpression == child) {
			fExpression = (IASTExpression) other;
			other.setParent(child.getParent());
			other.setPropertyInParent(child.getPropertyInParent());
		} else if (child instanceof IASTTypeId && other instanceof IASTTypeId && fTypeId == child) {
			fTypeId = (IASTTypeId) other;
			other.setParent(child.getParent());
			other.setPropertyInParent(child.getPropertyInParent());
		}
	}
}
