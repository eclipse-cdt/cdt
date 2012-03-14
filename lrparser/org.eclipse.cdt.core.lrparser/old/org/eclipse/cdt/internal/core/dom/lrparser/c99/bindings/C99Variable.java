/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.core.runtime.PlatformObject;

@SuppressWarnings("restriction")
public class C99Variable extends PlatformObject implements IC99Binding, IVariable, ITypeable {
	private boolean isAuto;
	private boolean isExtern;
	private boolean isRegister;
	private boolean isStatic;
	
	private String name;
	private IType type;
	
	private IScope scope;
	
	public C99Variable() {
	}
	
	public C99Variable(String name) {
		this.name = name;
	}
	
	public void setType(IType type) {
		this.type = type;
	}
	
	public IType getType() {
		return type;
	}
	
	public void setAuto(boolean auto) {
		this.isAuto = auto;
	}
	
	public boolean isAuto() {
		return isAuto;
	}
	
	public void setExtern(boolean extern) {
		this.isExtern = extern;
	}
	
	public boolean isExtern() {
		return isExtern;
	}
	
	public void setRegister(boolean isRegister) {
		this.isRegister = isRegister;
	}
	
	public boolean isRegister() {
		return isRegister;
	}
	
	public void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}
	
	public boolean isStatic()  {
		return isStatic;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public char[] getNameCharArray() {
		return name.toCharArray();
	}
	
	public ILinkage getLinkage() {
		return Linkage.C_LINKAGE;
	}

	public IScope getScope() {
		return scope;
	}

	public void setScope(IScope scope) {
		this.scope = scope;
	}

	public IBinding getOwner() {
		if (scope == null)
			return null;
		
		return CVisitor.findDeclarationOwner((IASTNode) scope.getScopeName(), true);
	}

	public IValue getInitialValue() {
		return null;
	}

	@Override
	public String toString() {
		return getName();
	}
}
