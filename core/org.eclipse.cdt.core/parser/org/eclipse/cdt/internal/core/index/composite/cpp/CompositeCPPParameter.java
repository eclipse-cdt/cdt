/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDelegateCreator;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCPPParameter extends CompositeCPPVariable implements ICPPParameter, ICPPDelegateCreator {
	public CompositeCPPParameter(ICompositesFactory cf, ICPPVariable rbinding) {
		super(cf, rbinding);
	}

	public boolean hasDefaultValue() {
		return ((ICPPParameter)rbinding).hasDefaultValue();
	}
	
	public ICPPDelegate createDelegate(IASTName name) {
		return new CPPParameter.CPPParameterDelegate(name, this);
	}
}
