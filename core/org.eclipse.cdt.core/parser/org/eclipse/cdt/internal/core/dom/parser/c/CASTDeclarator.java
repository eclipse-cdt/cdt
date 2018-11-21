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
 *     IBM Rational Software - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Yuan Zhang / Beth Tibbitts (IBM Research)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTAttributeOwner;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CASTDeclarator extends ASTAttributeOwner implements IASTDeclarator, IASTAmbiguityParent {
	private IASTInitializer initializer;
	private IASTName name;
	private IASTDeclarator nestedDeclarator;
	private IASTPointerOperator[] pointerOps;
	private int pointerOpsPos = -1;

	public CASTDeclarator() {
	}

	public CASTDeclarator(IASTName name) {
		setName(name);
	}

	public CASTDeclarator(IASTName name, IASTInitializer initializer) {
		setInitializer(initializer);
		setName(name);
	}

	@Override
	public CASTDeclarator copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTDeclarator copy(CopyStyle style) {
		return copy(new CASTDeclarator(), style);
	}

	protected <T extends CASTDeclarator> T copy(T copy, CopyStyle style) {
		copy.setName(name == null ? null : name.copy(style));
		copy.setInitializer(initializer == null ? null : initializer.copy(style));
		copy.setNestedDeclarator(nestedDeclarator == null ? null : nestedDeclarator.copy(style));
		for (IASTPointerOperator pointer : getPointerOperators()) {
			copy.addPointerOperator(pointer == null ? null : pointer.copy(style));
		}
		return super.copy(copy, style);
	}

	@Override
	public IASTPointerOperator[] getPointerOperators() {
		if (pointerOps == null)
			return IASTPointerOperator.EMPTY_ARRAY;
		pointerOps = ArrayUtil.trimAt(IASTPointerOperator.class, pointerOps, pointerOpsPos);
		return pointerOps;
	}

	@Override
	public IASTDeclarator getNestedDeclarator() {
		return nestedDeclarator;
	}

	@Override
	public IASTName getName() {
		return name;
	}

	@Override
	public IASTInitializer getInitializer() {
		return initializer;
	}

	@Override
	public void setInitializer(IASTInitializer initializer) {
		assertNotFrozen();
		this.initializer = initializer;
		if (initializer != null) {
			initializer.setParent(this);
			initializer.setPropertyInParent(INITIALIZER);
		}
	}

	@Override
	public void addPointerOperator(IASTPointerOperator operator) {
		assertNotFrozen();
		if (operator != null) {
			operator.setParent(this);
			operator.setPropertyInParent(POINTER_OPERATOR);
			pointerOps = ArrayUtil.appendAt(IASTPointerOperator.class, pointerOps, ++pointerOpsPos, operator);
		}
	}

	@Override
	public void setNestedDeclarator(IASTDeclarator nested) {
		assertNotFrozen();
		this.nestedDeclarator = nested;
		if (nested != null) {
			nested.setParent(this);
			nested.setPropertyInParent(NESTED_DECLARATOR);
		}
	}

	@Override
	public void setName(IASTName name) {
		assertNotFrozen();
		this.name = name;
		if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(DECLARATOR_NAME);
		}
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitDeclarators) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}

		for (int i = 0; i <= pointerOpsPos; i++) {
			if (!pointerOps[i].accept(action))
				return false;
		}

		if (!acceptByAttributeSpecifiers(action))
			return false;

		if (getPropertyInParent() != IASTTypeId.ABSTRACT_DECLARATOR && nestedDeclarator == null) {
			if (getParent() instanceof IASTDeclarator) {
				IASTDeclarator outermostDeclarator = (IASTDeclarator) getParent();
				while (outermostDeclarator.getParent() instanceof IASTDeclarator)
					outermostDeclarator = (IASTDeclarator) outermostDeclarator.getParent();
				if (outermostDeclarator.getPropertyInParent() != IASTTypeId.ABSTRACT_DECLARATOR && name != null
						&& !name.accept(action)) {
					return false;
				}
			} else if (name != null && !name.accept(action)) {
				return false;
			}
		}
		if (nestedDeclarator != null && !nestedDeclarator.accept(action)) {
			return false;
		}

		if (!postAccept(action))
			return false;

		if (action.shouldVisitDeclarators && action.leave(this) == ASTVisitor.PROCESS_ABORT) {
			return false;
		}
		return true;
	}

	protected boolean postAccept(ASTVisitor action) {
		if (initializer != null && !initializer.accept(action))
			return false;

		return true;
	}

	@Override
	public int getRoleForName(IASTName n) {
		if (n == this.name) {
			IASTNode getParent = getParent();
			boolean fnDtor = (this instanceof IASTFunctionDeclarator);
			if (getParent instanceof IASTDeclaration) {
				if (getParent instanceof IASTFunctionDefinition)
					return r_definition;
				if (getParent instanceof IASTSimpleDeclaration) {
					IASTSimpleDeclaration sd = (IASTSimpleDeclaration) getParent;
					int storage = sd.getDeclSpecifier().getStorageClass();
					if (getInitializer() != null || storage == IASTDeclSpecifier.sc_typedef)
						return r_definition;

					if (storage == IASTDeclSpecifier.sc_extern || storage == IASTDeclSpecifier.sc_static) {
						return r_declaration;
					}

					return fnDtor ? r_declaration : r_definition;
				}
			}
			if (getParent instanceof IASTTypeId)
				return r_reference;
			if (getParent instanceof IASTDeclarator) {
				IASTNode t = getParent;
				while (t instanceof IASTDeclarator)
					t = t.getParent();
				if (t instanceof IASTDeclaration) {
					if (getParent instanceof IASTFunctionDefinition)
						return r_definition;
					if (getParent instanceof IASTSimpleDeclaration) {
						if (getInitializer() != null)
							return r_definition;
						IASTSimpleDeclaration sd = (IASTSimpleDeclaration) getParent;
						int storage = sd.getDeclSpecifier().getStorageClass();
						if (storage == IASTDeclSpecifier.sc_extern || storage == IASTDeclSpecifier.sc_static) {
							return r_declaration;
						}
					}
					return fnDtor ? r_declaration : r_definition;
				}
				if (t instanceof IASTTypeId)
					return r_reference;
			}

			if (getParent instanceof IASTParameterDeclaration)
				return (n.toCharArray().length > 0) ? r_definition : r_declaration;
		}
		return r_unclear;
	}

	@Override
	public void replace(IASTNode child, IASTNode other) {
		if (child == nestedDeclarator) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			nestedDeclarator = (IASTDeclarator) other;
		}
	}
}
