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
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;

/**
 * The CPPImplicitFunction is used to represent implicit functions that exist on the translation
 * unit but are not actually part of the physical AST created by CDT.
 *
 * An example is GCC built-in functions.
 */
public class CPPImplicitFunction extends CPPFunction {
	private ICPPParameter[] params;
	private IScope scope;
	private ICPPFunctionType functionType;
	private final boolean isConstexpr;
	private final boolean takesVarArgs;
	private boolean isDeleted;
	private final char[] name;

	public CPPImplicitFunction(char[] name, IScope scope, ICPPFunctionType type, ICPPParameter[] params,
			boolean isConstexpr, boolean takesVarArgs) {
		super(null);
		this.name = name;
		this.scope = scope;
		this.functionType = type;
		this.params = params;
		this.takesVarArgs = takesVarArgs;
		this.isConstexpr = isConstexpr;
	}

	@Override
	public ICPPParameter[] getParameters() {
		return params;
	}

	@Override
	public ICPPFunctionType getType() {
		return functionType;
	}

	@Override
	public ICPPFunctionType getDeclaredType() {
		return functionType;
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

	@Override
	public IScope getFunctionScope() {
		return null;
	}

	@Override
	public boolean isConstexpr() {
		return isConstexpr;
	}

	@Override
	public boolean takesVarArgs() {
		return takesVarArgs;
	}

	@Override
	public boolean isDeleted() {
		return isDeleted;
	}

	@Override
	public IBinding getOwner() {
		return null;
	}

	public void setDeleted(boolean val) {
		isDeleted = val;
	}
}
