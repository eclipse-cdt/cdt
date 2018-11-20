/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;

/**
 * Used to represent built-in variables that exist on the translation
 * unit but are not actually part of the physical AST created by CDT.
 *
 * An example is the built-in variable __func__.
 */
public class CPPBuiltinVariable extends CPPVariable {
	private IType type;
	private char[] name;
	private IScope scope;

	public CPPBuiltinVariable(IType type, char[] name, IScope scope) {
		super(null);
		this.type = type;
		this.name = name;
		this.scope = scope;
	}

	@Override
	public IType getType() {
		return type;
	}

	@Override
	public String getName() {
		return String.valueOf(name);
	}

	@Override
	public char[] getNameCharArray() {
		return name;
	}

	@Override
	public IScope getScope() {
		return scope;
	}

	/**
	 * Returns null.
	 */
	@Override
	public IASTName[] getDeclarations() {
		return null;
	}

	/**
	 * Returns null.
	 */
	@Override
	public IASTNode getDefinition() {
		return null;
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void addDefinition(IASTNode node) {
		// do nothing
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void addDeclaration(IASTNode node) {
		// do nothing
	}

	@Override
	public String[] getQualifiedName() {
		String[] temp = new String[1];
		temp[0] = String.valueOf(name);

		return temp;
	}

	@Override
	public char[][] getQualifiedNameCharArray() {
		char[][] temp = new char[1][];
		temp[0] = name;

		return temp;
	}

	/**
	 * Returns true.
	 */
	@Override
	public boolean isGloballyQualified() {
		return true;
	}

	@Override
	public IBinding getOwner() {
		return null;
	}
}
