/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.core.runtime.PlatformObject;

public class CPPCompositeBinding extends PlatformObject implements IBinding {
	IBinding[] bindings;

	public CPPCompositeBinding(IBinding[] bindingList) {
		bindings = ArrayUtil.trim(bindingList, true);
	}
	
	@Override
	public String getName() {
		return bindings[0].getName();
	}

	@Override
	public char[] getNameCharArray() {
		return bindings[0].getNameCharArray();
	}

	@Override
	public IScope getScope() throws DOMException {
		return bindings[0].getScope();
	}

	@Override
	public IBinding getOwner() {
		return bindings[0].getOwner();
	}
	
	public IASTNode getPhysicalNode() {
		return null;
	}

	public IBinding[] getBindings() {
		return bindings;
	}

	@Override
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}
}
