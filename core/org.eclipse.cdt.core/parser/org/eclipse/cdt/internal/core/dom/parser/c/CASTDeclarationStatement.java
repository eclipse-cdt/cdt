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
 *     John Camelon (IBM Rational Software) - Initial API and implementation
 *     Yuan Zhang / Beth Tibbitts (IBM Research)
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTAttribute;
import org.eclipse.cdt.core.dom.ast.IASTAttributeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * A declaration statement.
 */
public class CASTDeclarationStatement extends ASTNode implements IASTDeclarationStatement, IASTAmbiguityParent {
	private IASTDeclaration declaration;

	public CASTDeclarationStatement() {
	}

	public CASTDeclarationStatement(IASTDeclaration declaration) {
		setDeclaration(declaration);
	}

	@Override
	public CASTDeclarationStatement copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTDeclarationStatement copy(CopyStyle style) {
		CASTDeclarationStatement copy = new CASTDeclarationStatement();
		copy.setDeclaration(declaration == null ? null : declaration.copy(style));
		return copy(copy, style);
	}

	@Override
	public IASTDeclaration getDeclaration() {
		return declaration;
	}

	@Override
	public void setDeclaration(IASTDeclaration declaration) {
		assertNotFrozen();
		this.declaration = declaration;
		if (declaration != null) {
			declaration.setParent(this);
			declaration.setPropertyInParent(DECLARATION);
		}
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitStatements) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}

		if (declaration != null && !declaration.accept(action))
			return false;

		if (action.shouldVisitStatements) {
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

	@Override
	public void replace(IASTNode child, IASTNode other) {
		if (child == declaration) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			declaration = (IASTDeclaration) other;
		}
	}

	@Override
	public IASTAttribute[] getAttributes() {
		// Declaration statements don't have attributes.
		return IASTAttribute.EMPTY_ATTRIBUTE_ARRAY;
	}

	@Override
	public void addAttribute(IASTAttribute attribute) {
		// Declaration statements don't have attributes.
		throw new UnsupportedOperationException();
	}

	@Override
	public IASTAttributeSpecifier[] getAttributeSpecifiers() {
		// Declaration statements don't have attributes.
		return IASTAttributeSpecifier.EMPTY_ATTRIBUTE_SPECIFIER_ARRAY;
	}

	@Override
	public void addAttributeSpecifier(IASTAttributeSpecifier attributeSpecifier) {
		// Declaration statements don't have attributes.
		throw new UnsupportedOperationException();
	}
}
