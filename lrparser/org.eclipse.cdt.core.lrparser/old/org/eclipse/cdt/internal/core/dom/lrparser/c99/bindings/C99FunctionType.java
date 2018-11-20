/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
package org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IType;

public class C99FunctionType implements IFunctionType {

	private IType returnType;
	private List<IType> parameterTypes = new ArrayList<>();

	@Override
	public IType[] getParameterTypes() {
		return parameterTypes.toArray(new IType[parameterTypes.size()]);
	}

	public void addParameterType(IType parameterType) {
		parameterTypes.add(parameterType);
	}

	@Override
	public IType getReturnType() {
		return returnType;
	}

	public void setReturnType(IType returnType) {
		this.returnType = returnType;
	}

	@Override
	public boolean isSameType(@SuppressWarnings("unused") IType type) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public C99FunctionType clone() {
		try {
			C99FunctionType clone = (C99FunctionType) super.clone();
			clone.setReturnType((IType) returnType.clone());
			clone.parameterTypes = new ArrayList<>();
			for (IType parameterType : parameterTypes) {
				clone.addParameterType((IType) parameterType.clone());
			}
			return clone;
		} catch (CloneNotSupportedException e) {
			assert false;
			return null;
		}

	}

	@Override
	public boolean takesVarArgs() {
		// Not implemented
		return false;
	}
}
