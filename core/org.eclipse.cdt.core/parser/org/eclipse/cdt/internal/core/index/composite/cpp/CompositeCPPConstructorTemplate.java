/*******************************************************************************
 * Copyright (c) 2007, 2010 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPExecution;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

public class CompositeCPPConstructorTemplate extends CompositeCPPMethodTemplate implements ICPPConstructor {

	public CompositeCPPConstructorTemplate(ICompositesFactory cf, ICPPConstructor rbinding) {
		super(cf, rbinding);
	}

	@Override
	public ICPPExecution getConstructorChainExecution(IASTNode point) {
		return ((ICPPConstructor) rbinding).getConstructorChainExecution(point);
	}
}
