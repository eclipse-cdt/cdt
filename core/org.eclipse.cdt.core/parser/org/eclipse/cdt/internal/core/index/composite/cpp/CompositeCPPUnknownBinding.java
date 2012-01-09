/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCPPUnknownBinding extends CompositeCPPBinding implements ICPPUnknownBinding {
	public CompositeCPPUnknownBinding(ICompositesFactory cf, ICPPUnknownBinding rbinding) {
		super(cf, rbinding);
	}

	@Override
	public Object clone() {
		fail(); return null;
	}

	@Override
	public ICPPScope asScope() {
    	return null;
    }

	@Override
	public IASTName getUnknownName() {
		return ((ICPPUnknownBinding) rbinding).getUnknownName();
	}
}
