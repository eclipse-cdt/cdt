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
 *    Markus Schorn - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

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
public class CBuiltinVariable extends CVariable {
	private IType type = null;
	private char[] name = null;
	private IScope scope = null;

	public CBuiltinVariable(IType type, char[] name, IScope scope) {
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
	 * returns null
	 */
	@Override
	public IASTNode[] getDeclarations() {
		return null;
	}

	/**
	 * returns null
	 */
	@Override
	public IASTNode getDefinition() {
		return null;
	}

	@Override
	public IBinding getOwner() {
		return null;
	}
}
