/*******************************************************************************
 * Copyright (c) 2007, 2009 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Andrew Ferguson (Symbian) - Initial implementation
 *   Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCPPParameter extends CompositeCPPVariable implements ICPPParameter {
	public CompositeCPPParameter(ICompositesFactory cf, ICPPVariable rbinding) {
		super(cf, rbinding);
	}

	@Override
	public boolean hasDefaultValue() {
		return ((ICPPParameter)rbinding).hasDefaultValue();
	}

	@Override
	public boolean isParameterPack() {
		return ((ICPPParameter)rbinding).isParameterPack();
	}
}
