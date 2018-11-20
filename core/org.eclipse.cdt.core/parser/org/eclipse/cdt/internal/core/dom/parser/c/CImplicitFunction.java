/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;

/**
 * The CImplicitFunction is used to represent implicit functions that exist on the translation
 * unit but are not actually part of the physical AST created by CDT.
 *
 * An example is GCC built-in functions.
 *
 * @author dsteffle
 */
public class CImplicitFunction extends CExternalFunction {

	private IParameter[] parms = null;
	private IScope scope = null;
	private boolean takesVarArgs = false;
	private char[] name = null;

	public CImplicitFunction(char[] name, IScope scope, IFunctionType type, IParameter[] parms, boolean takesVarArgs) {
		super(null, null);
		this.name = name;
		this.scope = scope;
		this.type = type;
		this.parms = parms;
		this.takesVarArgs = takesVarArgs;
	}

	@Override
	public IParameter[] getParameters() {
		return parms;
	}

	@Override
	public IFunctionType getType() {
		return type;
	}

	@Override
	public boolean takesVarArgs() {
		return takesVarArgs;
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

}
