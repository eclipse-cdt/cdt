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
 *     Markus Schorn (Wind River Systems)
 *     Yuan Zhang / Beth Tibbitts (IBM Research)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTAttributeOwner;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * Models a simple declaration.
 */
public class CASTSimpleDeclaration extends ASTAttributeOwner implements IASTSimpleDeclaration, IASTAmbiguityParent {
	private IASTDeclarator[] declarators;
	private int declaratorsPos = -1;
	private IASTDeclSpecifier declSpecifier;

	public CASTSimpleDeclaration() {
	}

	public CASTSimpleDeclaration(IASTDeclSpecifier declSpecifier) {
		setDeclSpecifier(declSpecifier);
	}

	@Override
	public CASTSimpleDeclaration copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTSimpleDeclaration copy(CopyStyle style) {
		CASTSimpleDeclaration copy = new CASTSimpleDeclaration();
		copy.setDeclSpecifier(declSpecifier == null ? null : declSpecifier.copy(style));
		for (IASTDeclarator declarator : getDeclarators()) {
			copy.addDeclarator(declarator == null ? null : declarator.copy(style));
		}
		return copy(copy, style);
	}

	@Override
	public IASTDeclSpecifier getDeclSpecifier() {
		return declSpecifier;
	}

	@Override
	public IASTDeclarator[] getDeclarators() {
		if (declarators == null)
			return IASTDeclarator.EMPTY_DECLARATOR_ARRAY;
		declarators = ArrayUtil.trimAt(IASTDeclarator.class, declarators, declaratorsPos);
		return declarators;
	}

	@Override
	public void addDeclarator(IASTDeclarator d) {
		assertNotFrozen();
		if (d != null) {
			d.setParent(this);
			d.setPropertyInParent(DECLARATOR);
			declarators = ArrayUtil.appendAt(IASTDeclarator.class, declarators, ++declaratorsPos, d);
		}
	}

	@Override
	public void setDeclSpecifier(IASTDeclSpecifier declSpecifier) {
		assertNotFrozen();
		this.declSpecifier = declSpecifier;
		if (declSpecifier != null) {
			declSpecifier.setParent(this);
			declSpecifier.setPropertyInParent(DECL_SPECIFIER);
		}
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitDeclarations) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}

		if (!acceptByAttributeSpecifiers(action))
			return false;
		if (declSpecifier != null && !declSpecifier.accept(action))
			return false;

		IASTDeclarator[] dtors = getDeclarators();
		for (int i = 0; i < dtors.length; i++) {
			if (!dtors[i].accept(action))
				return false;
		}

		if (action.shouldVisitDeclarations) {
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
		if (declSpecifier == child) {
			other.setParent(child.getParent());
			other.setPropertyInParent(child.getPropertyInParent());
			declSpecifier = (IASTDeclSpecifier) other;
		} else {
			IASTDeclarator[] declarators = getDeclarators();
			for (int i = 0; i < declarators.length; i++) {
				if (declarators[i] == child) {
					declarators[i] = (IASTDeclarator) other;
					other.setParent(child.getParent());
					other.setPropertyInParent(child.getPropertyInParent());
					break;
				}
			}
		}
	}
}