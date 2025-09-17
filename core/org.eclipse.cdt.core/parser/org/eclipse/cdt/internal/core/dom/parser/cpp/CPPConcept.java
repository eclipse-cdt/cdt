/*******************************************************************************
 * Copyright (c) 2025 Igor V. Kovalenko.
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
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConceptDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConcept;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.PlatformObject;

public class CPPConcept extends PlatformObject implements ICPPConcept {
	protected ICPPASTConceptDefinition definition;
	private ICPPTemplateParameter[] templateParameters;

	public CPPConcept(ICPPASTConceptDefinition definition) {
		this.definition = definition;
	}

	private IASTName getTemplateName() {
		return getASTName();
	}

	@Override
	public ICPPTemplateParameter[] getTemplateParameters() {
		if (templateParameters == null) {
			ICPPTemplateParameter[] result = ICPPTemplateParameter.EMPTY_TEMPLATE_PARAMETER_ARRAY;
			ICPPASTTemplateDeclaration template = CPPTemplates.getTemplateDeclaration(getTemplateName());
			if (template != null) {
				ICPPASTTemplateParameter[] params = template.getTemplateParameters();
				for (ICPPASTTemplateParameter param : params) {
					IBinding p = CPPTemplates.getTemplateParameterName(param).resolveBinding();
					if (p instanceof ICPPTemplateParameter) {
						result = ArrayUtil.append(result, (ICPPTemplateParameter) p);
					}
				}
			}
			templateParameters = ArrayUtil.trim(result);
		}
		return templateParameters;
	}

	@Override
	public ICPPASTConceptDefinition getConceptDefinition() {
		return definition;
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
	public String getName() {
		return getASTName().toString();
	}

	@Override
	public char[] getNameCharArray() {
		return getASTName().getSimpleID();
	}

	protected IASTName getASTName() {
		return definition.getName();
	}

	@Override
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}

	@Override
	public IBinding getOwner() {
		return CPPVisitor.findNameOwner(getASTName(), false);
	}

	@Override
	public IScope getScope() throws DOMException {
		return CPPVisitor.getContainingScope(getASTName());
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("concept "); //$NON-NLS-1$
		result.append(getName());
		return result.toString();
	}
}
