/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.core.runtime.PlatformObject;

@SuppressWarnings("restriction")
public class C99Function extends PlatformObject implements IC99Binding, IFunction, ITypeable {
	private String name;
	private IFunctionType type;
	private List<IParameter> parameters = new ArrayList<IParameter>();
	
	private boolean isAuto;
	private boolean isExtern;
	private boolean isInline;
	private boolean isRegister;
	private boolean isStatic;
	private boolean isVarArgs;
	private boolean isNoReturn;
	
	// the scope that the function is in (must be the global scope, no?)
	private IScope scope;
	
	// the scope that represents the body of the function
	private IScope bodyScope;

	public C99Function() {
	}
	
	public C99Function(String name) {
		this.name = name;
	}
	
	public C99Function(String name, IFunctionType type) {
		this(name);
		this.type = type;
	}

	@Override
	public IParameter[] getParameters() {
		return parameters.toArray(new IParameter[parameters.size()]);
	}
	
	public void addParameter(IParameter parameter) {
		parameters.add(parameter);
	}

	@Override
	public IFunctionType getType() {
		return type;
	}
	
	public void setType(IFunctionType type) {
		this.type = type;
	}

	@Override
	public boolean isAuto() {
		return isAuto;
	}

	public void setAuto(boolean isAuto) {
		this.isAuto = isAuto;
	}

	@Override
	public boolean isExtern() {
		return isExtern;
	}

	public void setExtern(boolean isExtern) {
		this.isExtern = isExtern;
	}

	@Override
	public boolean isInline() {
		return isInline;
	}

	public void setInline(boolean isInline) {
		this.isInline = isInline;
	}

	@Override
	public boolean isRegister() {
		return isRegister;
	}

	public void setRegister(boolean isRegister) {
		this.isRegister = isRegister;
	}

	@Override
	public boolean isStatic() {
		return isStatic;
	}

	public void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}

	@Override
	public boolean takesVarArgs() {
		return isVarArgs;
	}

	public void setTakesVarArgs(boolean isVarArgs) {
		this.isVarArgs = isVarArgs;
	}

	@Override
	public boolean isNoReturn() {
		return isNoReturn;
	}

	public void setNoReturn(boolean isNoReturn) {
		this.isNoReturn = isNoReturn;
	}

	@Override
	public ILinkage getLinkage() {
		return Linkage.C_LINKAGE;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public char[] getNameCharArray() {
		return name.toCharArray();
	}

	@Override
	public IScope getScope() {
		return scope;
	}
	
	@Override
	public IScope getFunctionScope() {
		return bodyScope;
	}
	
	public void setFunctionScope(IScope bodyScope) {
		this.bodyScope = bodyScope;
	}

	@Override
	public void setScope(IScope scope) {
		this.scope = scope;
	}

	@Override
	public IBinding getOwner() {
		return null;
	}
}
