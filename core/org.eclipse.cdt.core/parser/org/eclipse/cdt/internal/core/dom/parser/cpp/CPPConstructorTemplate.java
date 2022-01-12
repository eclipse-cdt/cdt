/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;

public class CPPConstructorTemplate extends CPPMethodTemplate implements ICPPConstructor {
	public CPPConstructorTemplate(IASTName name) {
		super(name);
	}

	@Override
	public ICPPExecution getConstructorChainExecution() {
		if (!isConstexpr()) {
			return null;
		}
		return CPPConstructor.computeConstructorChainExecution(getDefinition());
	}

	@Override
	public ICPPExecution getConstructorChainExecution(IASTNode point) {
		return getConstructorChainExecution();
	}
}
