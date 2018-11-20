/*******************************************************************************
 * Copyright (c) 2017 Nathan Ridge.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

public class CompositeCPPAliasTemplateSpecialization extends CompositeCPPAliasTemplate implements ICPPSpecialization {
	public CompositeCPPAliasTemplateSpecialization(ICompositesFactory cf, ICPPAliasTemplate delegate) {
		super(cf, delegate);
	}

	@Override
	public IBinding getSpecializedBinding() {
		return TemplateInstanceUtil.getSpecializedBinding(cf, rbinding);
	}

	@Override
	public ICPPTemplateParameterMap getTemplateParameterMap() {
		IBinding owner = getOwner();
		if (owner instanceof ICPPSpecialization) {
			return ((ICPPSpecialization) owner).getTemplateParameterMap();
		}
		return CPPTemplateParameterMap.EMPTY;
	}
}
