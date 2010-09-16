/*
 * CPPFunctionSet.java
 * Created on Sep 13, 2010
 *
 * Copyright 2010 Wind River Systems, Inc. All rights reserved.
 */

package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNameBase;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPTwoPhaseBinding;

/**
 * Used as intermediate binding for names nominating a function without calling it. 
 * The actual function can be resolved in certain contexts.
 */
public class CPPFunctionSet implements ICPPTwoPhaseBinding {

	final ICPPFunction[] fBindings;
	
	public CPPFunctionSet(ICPPFunction[] bindingList) {
		fBindings = ArrayUtil.removeNulls(bindingList);
	}
	
	public String getName() {
		return fBindings[0].getName();
	}

	public char[] getNameCharArray() {
		return fBindings[0].getNameCharArray();
	}

	public IScope getScope() throws DOMException {
		return fBindings[0].getScope();
	}

	public IBinding getOwner() {
		return fBindings[0].getOwner();
	}
	
	public ICPPFunction[] getBindings() {
		return fBindings;
	}

	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}

	public IBinding resolveFinalBinding(CPPASTNameBase astName) {
		return CPPSemantics.resolveTargetedFunction(astName, fBindings);
	}

	
	@SuppressWarnings("unchecked")
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (adapter.isAssignableFrom(getClass())) 
			return this;
		return null;
	}
}
