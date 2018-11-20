/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.lrparser.c99.action.deprecated;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings.C99PointerType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;

import lpg.lpgjavaruntime.IToken;

/**
 * Represents a frame on the declaration stack used by the resolver actions.
 *
 * TODO: document this class better
 *
 * @author Mike Kucera
 */
@SuppressWarnings("restriction")
public class DeclaratorFrame {
	private DeclSpec declSpec;
	//IBinding declarator;
	private IToken declaratorName;
	private boolean isDeclaratorBracketed;
	private boolean isFunctionDeclarator = false;

	// temporary storage for pointer modifiers
	private LinkedList<LinkedList<C99PointerType>> pointerModifiers = new LinkedList<>();

	// stores pointer and array modifiers that are applied to the declarator
	private LinkedList<ITypeContainer> typeModifiers = new LinkedList<>();

	private LinkedList<IBinding> nestedDeclarations = new LinkedList<>();

	public DeclaratorFrame() {
	}

	public DeclaratorFrame(DeclSpec declSpec) {
		this.declSpec = declSpec;
	}

	public DeclSpec getDeclSpec() {
		if (declSpec == null)
			declSpec = new DeclSpec();

		return declSpec;
	}

	public IType getDeclaratorType() {
		// the declSpec may be null, so use getDeclSpec()
		IType baseType = getDeclSpec().getType();

		if (typeModifiers.isEmpty())
			return baseType;

		IType type = typeModifiers.get(0);

		// link the types together
		for (int i = 1; i < typeModifiers.size(); i++) {
			ITypeContainer t1 = typeModifiers.get(i - 1);
			ITypeContainer t2 = typeModifiers.get(i);
			t1.setType(t2);
		}

		ITypeContainer last = typeModifiers.get(typeModifiers.size() - 1);
		last.setType(baseType);
		return type;
	}

	public IToken getDeclaratorName() {
		return declaratorName;
	}

	public void setDeclaratorName(IToken declaratorName) {
		this.declaratorName = declaratorName;
	}

	public boolean isDeclaratorBracketed() {
		return isDeclaratorBracketed;
	}

	public void setDeclaratorBracketed(boolean isDeclaratorBracketed) {
		this.isDeclaratorBracketed = isDeclaratorBracketed;
	}

	public boolean isFunctionDeclarator() {
		return isFunctionDeclarator;
	}

	public void setFunctionDeclarator(boolean isFunctionDeclarator) {
		this.isFunctionDeclarator = isFunctionDeclarator;
	}

	public List<IBinding> getNestedDeclarations() {
		return nestedDeclarations;
	}

	public void addNestedDeclaration(IBinding binding) {
		nestedDeclarations.add(binding);
	}

	public void removeLastNestedDeclaration() {
		nestedDeclarations.removeLast();
	}

	public void addTypeModifier(ITypeContainer x) {
		typeModifiers.add(x);
	}

	public void removeLastTypeModifier() {
		typeModifiers.removeLast();
	}

	public void addPointerModifier(C99PointerType x) {
		pointerModifiers.getLast().add(x);
	}

	public void removeLastPointerModifier() {
		pointerModifiers.getLast().removeLast();
	}

	public void openPointerModifierScope() {
		pointerModifiers.add(new LinkedList<C99PointerType>());
	}

	public void openPointerModifierScope(LinkedList<C99PointerType> scope) {
		pointerModifiers.add(scope);
	}

	public LinkedList<C99PointerType> closePointerModifierScope() {
		return pointerModifiers.removeLast();
	}
}
