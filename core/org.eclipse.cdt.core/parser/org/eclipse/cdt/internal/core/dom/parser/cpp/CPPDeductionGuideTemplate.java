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

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;

/**
 * Represents c++17 deduction guide template.
 */
public class CPPDeductionGuideTemplate extends CPPDeductionGuide
		implements ICPPTemplateDefinition, ICPPTemplateParameterOwner {

	public CPPDeductionGuideTemplate(IASTDeclarator fnDecl, ICPPFunction functionBinding) {
		super(fnDecl, functionBinding);
	}

	@Override
	public ICPPTemplateParameter[] getTemplateParameters() {
		if (functionBinding instanceof ICPPTemplateDefinition template) {
			return template.getTemplateParameters();
		}
		return null;
	}

	@Override
	public IBinding resolveTemplateParameter(ICPPTemplateParameter param) {
		if (functionBinding instanceof ICPPTemplateParameterOwner owner) {
			return owner.resolveTemplateParameter(param);
		}
		return null;
	}
}
