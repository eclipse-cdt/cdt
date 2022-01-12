/*******************************************************************************
 * Copyright (c) 2017 Nathan Ridge.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Binding for implicit template type parameters.
 *
 * Used for the template type parameters of implicit method templates.
 */
public class CPPImplicitTemplateTypeParameter extends PlatformObject
		implements ICPPTemplateTypeParameter, ICPPUnknownType, ICPPUnknownBinding {
	private int fParameterID;
	private boolean fIsParameterPack;
	private ICPPScope fUnknownScope;

	// The containing (implicit) template definition.
	private ICPPTemplateDefinition fContainingTemplate;

	// The AST node that triggered the creation of the implicit template.
	// For methods of generic lambdas, this is the lambda expression.
	private IASTNode fNode;

	public CPPImplicitTemplateTypeParameter(IASTNode node, int position, boolean isParameterPack) {
		fParameterID = computeParameterID(position);
		fIsParameterPack = isParameterPack;
		fNode = node;
	}

	private int computeParameterID(int position) {
		int nesting = 0;
		for (IASTNode node = fNode; node != null; node = node.getParent()) {
			if (node instanceof ICPPASTInternalTemplateDeclaration) {
				nesting = ((ICPPASTInternalTemplateDeclaration) node).getNestingLevel();
				break;
			}
		}
		return (nesting << 16) + (position & 0xffff);
	}

	public void setContainingTemplate(ICPPTemplateDefinition containingTemplate) {
		fContainingTemplate = containingTemplate;
	}

	@Override
	public String[] getQualifiedName() throws DOMException {
		return new String[] { getName() };
	}

	@Override
	public char[][] getQualifiedNameCharArray() throws DOMException {
		return new char[][] { getNameCharArray() };
	}

	@Override
	public boolean isGloballyQualified() throws DOMException {
		return false;
	}

	@Override
	public String getName() {
		return new String();
	}

	@Override
	public char[] getNameCharArray() {
		// Implicit template parameters are unnamed.
		return CharArrayUtils.EMPTY;
	}

	@Override
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}

	@Override
	public IBinding getOwner() {
		return fContainingTemplate;
	}

	@Override
	public IScope getScope() throws DOMException {
		// TODO: Do we need an implicit template scope for the implicit template
		//       parameter to live in?
		return CPPVisitor.getContainingScope(fNode);
	}

	@Override
	public short getParameterPosition() {
		return (short) fParameterID;
	}

	@Override
	public short getTemplateNestingLevel() {
		return (short) (fParameterID >> 16);
	}

	@Override
	public int getParameterID() {
		return fParameterID;
	}

	@Override
	public ICPPTemplateArgument getDefaultValue() {
		// Implicit template parameters do not have default arguments.
		return null;
	}

	@Override
	public boolean isParameterPack() {
		return fIsParameterPack;
	}

	@Override
	public IType getDefault() throws DOMException {
		return null;
	}

	@Override
	public boolean isSameType(IType type) {
		if (type == this)
			return true;
		if (type instanceof ITypedef)
			return type.isSameType(this);
		if (!(type instanceof ICPPTemplateTypeParameter))
			return false;

		return getParameterID() == ((ICPPTemplateParameter) type).getParameterID();
	}

	@Override
	public Object clone() {
		return new CPPImplicitTemplateTypeParameter(fNode, getParameterPosition(), fIsParameterPack);
	}

	@Override
	public ICPPScope asScope() throws DOMException {
		if (fUnknownScope == null) {
			fUnknownScope = new CPPUnknownTypeScope(this, null);
		}
		return fUnknownScope;
	}
}
