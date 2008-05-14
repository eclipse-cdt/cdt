/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

public class CompositeCPPTemplateTypeParameter extends CompositeCPPBinding 
	implements ICPPTemplateTypeParameter, ICPPUnknownBinding, IIndexType {

	public CompositeCPPTemplateTypeParameter(ICompositesFactory cf,	ICPPTemplateTypeParameter binding) {
		super(cf, binding);
	}

	public IType getDefault() throws DOMException {
		IIndexType preresult= (IIndexType) ((ICPPTemplateTypeParameter)rbinding).getDefault();
		return cf.getCompositeType(preresult);
	}

	public boolean isSameType(IType type) {
		return ((IType)rbinding).isSameType(type);
	}
	
	@Override
	public Object clone() {
		fail(); return null; 
	}

	public ICPPScope getUnknownScope() {
		return null;
	}

	public IBinding resolvePartially(ICPPUnknownBinding parentBinding, ObjectMap argMap, ICPPScope instantiationScope) {
    	// Cannot do resolution here since the result is not necessarily a binding.
		return null;
	}

	public IASTName getUnknownName() {
		return null;
	}

	public ICPPUnknownBinding getUnknownContainerBinding() {
		return null;
	}
}
