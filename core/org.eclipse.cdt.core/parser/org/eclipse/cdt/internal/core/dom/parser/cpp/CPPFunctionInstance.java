/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;

/**
 * An instantiation or an explicit specialization of a function template.
 */
public class CPPFunctionInstance extends CPPFunctionSpecialization implements ICPPFunctionInstance {
	private final ICPPTemplateArgument[] fArguments;

	public CPPFunctionInstance(ICPPFunction orig, IBinding owner, ICPPTemplateParameterMap argMap,
			ICPPTemplateArgument[] args, ICPPFunctionType type, IType[] exceptionSpecs) {
		super(orig, owner, argMap, type, exceptionSpecs);
		fArguments = args;
	}

	@Override
	public ICPPTemplateDefinition getTemplateDefinition() {
		return (ICPPTemplateDefinition) getSpecializedBinding();
	}

	@Override
	public ICPPTemplateArgument[] getTemplateArguments() {
		return fArguments;
	}

	@Override
	public boolean isExplicitSpecialization() {
		if (getDefinition() != null)
			return true;
		IASTNode[] decls = getDeclarations();
		if (decls != null) {
			for (IASTNode decl : decls) {
				if (decl != null)
					return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * For debug purposes only
	 */
	@Override
	public String toString() {
		return getName() + " " + ASTTypeUtil.getArgumentListString(fArguments, true); //$NON-NLS-1$
	}

	@Override
	public boolean equals(Object obj) {
		if ((obj instanceof ICPPTemplateInstance) && (obj instanceof ICPPFunction)) {
			final ICPPTemplateInstance inst = (ICPPTemplateInstance) obj;
			ICPPFunctionType ct1 = ((ICPPFunction) getSpecializedBinding()).getType();
			ICPPFunctionType ct2 = ((ICPPFunction) inst.getTemplateDefinition()).getType();
			if (!ct1.isSameType(ct2))
				return false;

			return CPPTemplates.haveSameArguments(this, inst);
		}

		return false;
	}
}
