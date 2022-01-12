/*************************************************************************
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
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructorSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;

/**
 * Specialization of a constructor for a class-template or class-template specialization.
 */
public class CPPConstructorSpecialization extends CPPMethodSpecialization implements ICPPConstructorSpecialization {

	public CPPConstructorSpecialization(ICPPConstructor orig, ICPPClassType owner, ICPPTemplateParameterMap argMap,
			ICPPFunctionType type, IType[] exceptionSpecs) {
		super(orig, owner, argMap, type, exceptionSpecs);
	}

	static <T extends ICPPConstructorSpecialization & ICPPInternalBinding> ICPPExecution getConstructorChainExecution(
			T functionSpec) {
		if (!functionSpec.isConstexpr()) {
			return null;
		}

		IASTNode def = functionSpec.getDefinition();
		if (def != null) {
			return CPPConstructor.computeConstructorChainExecution(def);
		}
		return CPPTemplates.instantiateConstructorChain(functionSpec);
	}

	@Override
	public ICPPExecution getConstructorChainExecution() {
		return getConstructorChainExecution(this);
	}

	@Override
	public ICPPExecution getConstructorChainExecution(IASTNode point) {
		return getConstructorChainExecution();
	}
}
