/*******************************************************************************
 * Copyright (c) 2023 Igor V. Kovalenko.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Igor V. Kovalenko - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDeductionGuide;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Represents c++17 deduction guide.
 */
public class CPPDeductionGuide extends PlatformObject implements ICPPDeductionGuide {
	protected IASTDeclarator definition;
	protected ICPPFunction functionBinding;

	public CPPDeductionGuide(IASTDeclarator fnDecl, ICPPFunction functionBinding) {
		this.definition = fnDecl;
		this.functionBinding = functionBinding;
	}

	@Override
	public ICPPFunction getFunctionBinding() {
		return functionBinding;
	}

	@Override
	public String[] getQualifiedName() throws DOMException {
		return functionBinding.getQualifiedName();
	}

	@Override
	public char[][] getQualifiedNameCharArray() throws DOMException {
		return functionBinding.getQualifiedNameCharArray();
	}

	@Override
	public boolean isGloballyQualified() throws DOMException {
		return functionBinding.isGloballyQualified();
	}

	@Override
	public String getName() {
		return functionBinding.getName();
	}

	@Override
	public char[] getNameCharArray() {
		return functionBinding.getNameCharArray();
	}

	@Override
	public ILinkage getLinkage() {
		return functionBinding.getLinkage();
	}

	@Override
	public IBinding getOwner() {
		return functionBinding.getOwner();
	}

	protected IASTName getASTName() {
		return definition.getName().getLastName();
	}

	@Override
	public IScope getScope() throws DOMException {
		return CPPVisitor.getContainingScope(getASTName());
	}
}
