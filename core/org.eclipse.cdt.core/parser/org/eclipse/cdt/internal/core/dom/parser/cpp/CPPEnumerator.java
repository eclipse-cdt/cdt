/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTEnumerator;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalEnumerationSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Binding for a C++ enumerator.
 */
public class CPPEnumerator extends PlatformObject implements ICPPInternalEnumerator, ICPPInternalBinding {
	private IASTName enumName;
	private IType internalType;

	/**
	 * @param enumerator
	 */
	public CPPEnumerator(IASTName enumerator) {
		this.enumName = enumerator;
		enumerator.setBinding(this);
	}

	@Override
	public IASTNode[] getDeclarations() {
		return null;
	}

	@Override
	public IASTNode getDefinition() {
		return enumName;
	}

	@Override
	public String getName() {
		return new String(getNameCharArray());
	}

	@Override
	public char[] getNameCharArray() {
		return enumName.getSimpleID();
	}

	@Override
	public IScope getScope() {
		return CPPVisitor.getContainingScope(enumName);
	}

	public IASTNode getPhysicalNode() {
		return enumName;
	}

	@Override
	public IType getType() {
		IASTEnumerator etor = (IASTEnumerator) enumName.getParent();
		IASTInternalEnumerationSpecifier enumSpec = (IASTInternalEnumerationSpecifier) etor.getParent();
		if (enumSpec.isValueComputationInProgress()) {
			// During value computation enumerators can be referenced only by initializer
			// expressions of other enumerators of the same enumeration. Return the internal type
			// of the enumerator ([dcl.enum] 7.2-5).
			if (internalType != null)
				return internalType;
			ICPPEnumeration binding = (ICPPEnumeration) enumSpec.getName().resolveBinding();
			IType fixedType = binding.getFixedType();
			return fixedType != null ? fixedType : ProblemType.UNKNOWN_FOR_EXPRESSION;
		}
		return (IType) enumSpec.getName().resolveBinding();
	}

	@Override
	public IType getInternalType() {
		if (internalType == null) {
			getValue(); // Trigger value and internal type computation.
		}
		return internalType;
	}

	/**
	 * Sets the internal type of the enumerator. The enumerator has this type between the opening
	 * and the closing braces of the enumeration ([dcl.enum] 7.2-5).
	 *
	 * @param type the integral type of the enumerator's initializing value
	 */
	public void setInternalType(IType type) {
		internalType = type;
	}

	@Override
	public String[] getQualifiedName() {
		return CPPVisitor.getQualifiedName(this);
	}

	@Override
	public char[][] getQualifiedNameCharArray() {
		return CPPVisitor.getQualifiedNameCharArray(this);
	}

	@Override
	public boolean isGloballyQualified() throws DOMException {
		IScope scope = getScope();
		while (scope != null) {
			if (scope instanceof ICPPBlockScope)
				return false;
			scope = scope.getParent();
		}
		return true;
	}

	@Override
	public void addDefinition(IASTNode node) {
	}

	@Override
	public void addDeclaration(IASTNode node) {
	}

	@Override
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}

	@Override
	public IBinding getOwner() {
		return CPPVisitor.findDeclarationOwner(enumName, true);
	}

	@Override
	public IValue getValue() {
		final IASTNode parent = enumName.getParent();
		if (parent instanceof ASTEnumerator)
			return ((ASTEnumerator) parent).getIntegralValue();

		return IntegralValue.UNKNOWN;
	}

	@Override
	public String toString() {
		return getName();
	}
}
