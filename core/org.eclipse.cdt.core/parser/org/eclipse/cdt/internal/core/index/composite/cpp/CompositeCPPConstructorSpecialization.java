/*******************************************************************************
 * Copyright (c) 2007, 2010 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPExecution;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

public class CompositeCPPConstructorSpecialization extends CompositeCPPMethodSpecialization implements ICPPConstructor {

	public CompositeCPPConstructorSpecialization(ICompositesFactory cf, ICPPConstructor cons) {
		super(cf, cons);
	}

	@Override
	@Deprecated
	public ICPPExecution getConstructorChainExecution(IASTNode point) {
		return getConstructorChainExecution();
	}

	@Override
	public ICPPExecution getConstructorChainExecution() {
		return ((ICPPConstructor) rbinding).getConstructorChainExecution();
	}
}
