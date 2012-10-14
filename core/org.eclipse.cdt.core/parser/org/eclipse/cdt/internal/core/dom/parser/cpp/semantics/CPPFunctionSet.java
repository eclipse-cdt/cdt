/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNameBase;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPDeferredFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPTwoPhaseBinding;

/**
 * Used as intermediate binding for names nominating a function without calling it. 
 * The actual function can be resolved in certain contexts.
 */
public class CPPFunctionSet implements ICPPTwoPhaseBinding {
	private final ICPPFunction[] fBindings;
	private final IASTName fName;
	private final ICPPTemplateArgument[] fTemplateArguments;
	
	public CPPFunctionSet(ICPPFunction[] bindingList, ICPPTemplateArgument[] args, IASTName name) {
		fBindings = ArrayUtil.removeNulls(bindingList);
		fTemplateArguments= args;
		fName= name;
	}
	
	@Override
	public String getName() {
		return fBindings[0].getName();
	}

	@Override
	public char[] getNameCharArray() {
		return fBindings[0].getNameCharArray();
	}

	@Override
	public IScope getScope() throws DOMException {
		return fBindings[0].getScope();
	}

	@Override
	public IBinding getOwner() {
		return fBindings[0].getOwner();
	}
	
	public ICPPFunction[] getBindings() {
		return fBindings;
	}

	@Override
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}

	@Override
	public IBinding resolveFinalBinding(CPPASTNameBase astName) {
		return CPPSemantics.resolveTargetedFunction(astName, this);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (adapter.isAssignableFrom(getClass())) 
			return this;
		return null;
	}

	public ICPPTemplateArgument[] getTemplateArguments() {
		return fTemplateArguments;
	}

	public void applySelectedFunction(ICPPFunction selectedFunction) {
		if (selectedFunction != null && fName != null) {
			fName.setBinding(selectedFunction);
		}
	}
	
	public void setToUnknown() {
		if (fName != null) {
			fName.setBinding(new CPPDeferredFunction(null, fName.toCharArray()));
		}
	}
}
