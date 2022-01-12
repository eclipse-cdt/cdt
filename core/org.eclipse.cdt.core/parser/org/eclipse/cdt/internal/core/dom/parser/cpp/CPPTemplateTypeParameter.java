/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * The standard template parameter (template<typename T> or template<class T>).
 */
public class CPPTemplateTypeParameter extends CPPTemplateParameter
		implements ICPPTemplateTypeParameter, ICPPUnknownType {
	private ICPPScope unknownScope;
	private final boolean fIsParameterPack;

	public CPPTemplateTypeParameter(IASTName name, boolean isPack) {
		super(name);
		fIsParameterPack = isPack;
	}

	@Override
	public final boolean isParameterPack() {
		return fIsParameterPack;
	}

	@Override
	public ICPPScope asScope() {
		if (unknownScope == null) {
			IASTName n = null;
			IASTNode[] nodes = getDeclarations();
			if (nodes != null && nodes.length > 0)
				n = (IASTName) nodes[0];
			unknownScope = new CPPUnknownTypeScope(this, n);
		}
		return unknownScope;
	}

	@Override
	public IType getDefault() {
		IASTName[] nds = getDeclarations();
		if (nds == null || nds.length == 0)
			return null;
		for (IASTName nd : nds) {
			if (nd != null) {
				IASTNode parent = nd.getParent();
				if (parent instanceof ICPPASTSimpleTypeTemplateParameter) {
					ICPPASTSimpleTypeTemplateParameter simple = (ICPPASTSimpleTypeTemplateParameter) parent;
					IASTTypeId typeId = simple.getDefaultType();
					if (typeId != null)
						return CPPVisitor.createType(typeId);
				}
			}
		}
		return null;
	}

	@Override
	public ICPPTemplateArgument getDefaultValue() {
		IType t = getDefault();
		if (t == null)
			return null;

		return new CPPTemplateTypeArgument(t);
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
}
