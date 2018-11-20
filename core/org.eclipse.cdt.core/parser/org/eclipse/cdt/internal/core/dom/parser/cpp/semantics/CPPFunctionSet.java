/*******************************************************************************
 * Copyright (c) 2010, 2015 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
		fTemplateArguments = args;
		fName = name;
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

	/**
	 * Returns the template arguments, or {@code null} if the function set doesn't represent a template
	 * specialization.
	 */
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
			fName.setBinding(new CPPDeferredFunction(null, fName.toCharArray(), fBindings));
		}
	}

	/** For debugging only */
	@Override
	public String toString() {
		if (fName != null)
			return fName.toString();
		try {
			return String.join("::", fBindings[0].getQualifiedName()); //$NON-NLS-1$
		} catch (DOMException e) {
			return super.toString();
		}
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof CPPFunctionSet)) {
			return false;
		}
		CPPFunctionSet o = (CPPFunctionSet) other;
		return CPPEvaluation.areEquivalentBindings(fBindings, o.fBindings) && fName == o.fName
				&& CPPEvaluation.areEquivalentArguments(fTemplateArguments, o.fTemplateArguments);
	}
}
