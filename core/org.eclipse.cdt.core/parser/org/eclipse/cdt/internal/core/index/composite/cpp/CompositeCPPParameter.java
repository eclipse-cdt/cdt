/*******************************************************************************
 * Copyright (c) 2007, 2014 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCPPParameter extends CompositeCPPVariable implements ICPPParameter {
	public CompositeCPPParameter(ICompositesFactory cf, ICPPVariable rbinding) {
		super(cf, rbinding);
	}

	@Override
	public boolean hasDefaultValue() {
		return ((ICPPParameter) rbinding).hasDefaultValue();
	}

	@Override
	public IValue getDefaultValue() {
		return cf.getCompositeValue(((ICPPParameter) rbinding).getDefaultValue());
	}

	@Override
	public boolean isParameterPack() {
		return ((ICPPParameter) rbinding).isParameterPack();
	}
}
