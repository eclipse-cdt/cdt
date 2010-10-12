/*******************************************************************************
 * Copyright (c) 2007, 2010 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCPPVariable extends CompositeCPPBinding implements ICPPVariable {

	public CompositeCPPVariable(ICompositesFactory cf, IVariable delegate) {
		super(cf, delegate);
	}
	
	public boolean isMutable() {
		return ((ICPPVariable)rbinding).isMutable();
	}

	public boolean isExternC() {
		return ((ICPPVariable)rbinding).isExternC();
	}

	public IType getType() {
		IType rtype = ((ICPPVariable)rbinding).getType();
		return cf.getCompositeType(rtype);
	}

	public boolean isAuto() {
		return ((ICPPVariable)rbinding).isAuto();
	}

	public boolean isExtern() {
		return ((ICPPVariable)rbinding).isExtern();
	}

	public boolean isRegister() {
		return ((ICPPVariable)rbinding).isRegister();
	}

	public boolean isStatic() {
		return ((ICPPVariable)rbinding).isStatic();
	}
	
	public IValue getInitialValue() {
		return ((ICPPVariable)rbinding).getInitialValue();
	}
}
