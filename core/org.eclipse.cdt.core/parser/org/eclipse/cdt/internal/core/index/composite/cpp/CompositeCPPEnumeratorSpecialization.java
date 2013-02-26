/*******************************************************************************
 * Copyright (c) 2013 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCPPEnumeratorSpecialization extends CompositeCPPEnumerator implements ICPPSpecialization {
	public CompositeCPPEnumeratorSpecialization(ICompositesFactory cf, IEnumerator delegate) {
		super(cf, delegate);
	}

	@Override
	public IBinding getSpecializedBinding() {
		return TemplateInstanceUtil.getSpecializedBinding(cf, rbinding);
	}	
	
	@Override
	public ICPPTemplateParameterMap getTemplateParameterMap() {
		IBinding owner= getOwner();
		if (owner instanceof ICPPSpecialization) {
			return ((ICPPSpecialization) owner).getTemplateParameterMap();
		}
		return CPPTemplateParameterMap.EMPTY;
	}

	@Override
	@Deprecated
	public ObjectMap getArgumentMap() {
		return TemplateInstanceUtil.getArgumentMap(cf, rbinding);
	}
}
