/*******************************************************************************
 * Copyright (c) 2012 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Thomas Corbat (IFS) - Initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.PlatformObject;

public class CPPAliasTemplate extends PlatformObject
		implements ICPPAliasTemplate, ICPPTemplateParameterOwner, ICPPInternalBinding {
	private final IASTName aliasName;
	private final IType aliasedType;
	private ICPPTemplateParameter[] templateParameters;

	public CPPAliasTemplate(IASTName aliasName, IType aliasedType) {
		this.aliasName = aliasName;
		this.aliasedType = aliasedType;
		aliasName.setBinding(this);
	}

	@Override
	public IType getType() {
		return aliasedType;
	}

	@Override
	public String getName() {
		return new String(getNameCharArray());
	}

	@Override
	public char[] getNameCharArray() {
		return aliasName.getSimpleID();
	}

	@Override
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}

	@Override
	public IBinding getOwner() {
		return CPPVisitor.findDeclarationOwner(aliasName, true);
	}

	@Override
	public IScope getScope() throws DOMException {
		return CPPVisitor.getContainingScope(aliasName.getParent());
	}

	@Override
	public boolean isSameType(IType type) {
		if (type == null) {
			return false;
		}
		IType aliasedType = getType();
		return type.isSameType(aliasedType);
	}

	@Override
	public Object clone() {
		IType t = null;
		try {
			t = (IType) super.clone();
		} catch (CloneNotSupportedException e) {
			// Not going to happen
		}
		return t;
	}

	@Override
	public String[] getQualifiedName() throws DOMException {
		return CPPVisitor.getQualifiedName(this);
	}

	@Override
	public char[][] getQualifiedNameCharArray() throws DOMException {
		return CPPVisitor.getQualifiedNameCharArray(this);
	}

	@Override
	public boolean isGloballyQualified() throws DOMException {
		return true;
	}

	@Override
	public ICPPTemplateParameter[] getTemplateParameters() {
		if (templateParameters == null) {
			ICPPASTTemplateDeclaration template = CPPTemplates.getTemplateDeclaration(aliasName);
			if (template == null)
				return ICPPTemplateParameter.EMPTY_TEMPLATE_PARAMETER_ARRAY;
			ICPPASTTemplateParameter[] params = template.getTemplateParameters();
			IBinding p = null;
			ICPPTemplateParameter[] result = null;
			for (ICPPASTTemplateParameter param : params) {
				p = CPPTemplates.getTemplateParameterName(param).resolveBinding();
				if (p instanceof ICPPTemplateParameter) {
					result = ArrayUtil.append(ICPPTemplateParameter.class, result, (ICPPTemplateParameter) p);
				}
			}
			templateParameters = ArrayUtil.trim(ICPPTemplateParameter.class, result);
		}
		return templateParameters;
	}

	@Override
	public IBinding resolveTemplateParameter(ICPPTemplateParameter templateParameter) {
		int pos = templateParameter.getParameterPosition();

		ICPPASTTemplateParameter[] params = CPPTemplates.getTemplateDeclaration(aliasName).getTemplateParameters();
		if (pos < params.length) {
			final IASTName oName = CPPTemplates.getTemplateParameterName(params[pos]);
			return oName.resolvePreBinding();
		}
		return templateParameter;
	}

	@Override
	public IASTNode getDefinition() {
		return aliasName;
	}

	@Override
	public IASTNode[] getDeclarations() {
		return null;
	}

	@Override
	public void addDefinition(IASTNode node) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addDeclaration(IASTNode node) {
		throw new UnsupportedOperationException();
	}

	/** For debugging only. */
	@Override
	public String toString() {
		return ASTTypeUtil.getQualifiedName(this) + " -> " + ASTTypeUtil.getType(aliasedType, true); //$NON-NLS-1$
	}
}
