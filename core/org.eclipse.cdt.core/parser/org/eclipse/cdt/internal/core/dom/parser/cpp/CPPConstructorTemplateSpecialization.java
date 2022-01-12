/*************************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructorSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;

/**
 * Specialization of a constructor template
 */
public class CPPConstructorTemplateSpecialization extends CPPMethodTemplateSpecialization
		implements ICPPConstructorSpecialization {

	public CPPConstructorTemplateSpecialization(ICPPConstructor original, ICPPClassSpecialization owner,
			ICPPTemplateParameterMap tpmap, ICPPFunctionType type, IType[] exceptionSpecs) {
		super(original, owner, tpmap, type, exceptionSpecs);
	}

	@Override
	public ICPPExecution getConstructorChainExecution() {
		return CPPConstructorSpecialization.getConstructorChainExecution(this);
	}

	@Override
	public ICPPExecution getConstructorChainExecution(IASTNode point) {
		return getConstructorChainExecution();
	}
}
