/*******************************************************************************
 * Copyright (c) 2007, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Bryan Wilkinson (QNX) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructorSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;

/**
 * Instantiation of a constructor template
 */
public class CPPConstructorInstance extends CPPMethodInstance implements ICPPConstructorSpecialization {
	public CPPConstructorInstance(ICPPConstructor orig, ICPPClassType owner, ICPPTemplateParameterMap tpmap,
			ICPPTemplateArgument[] args, ICPPFunctionType type, IType[] exceptionSpec) {
		super(orig, owner, tpmap, args, type, exceptionSpec);
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
