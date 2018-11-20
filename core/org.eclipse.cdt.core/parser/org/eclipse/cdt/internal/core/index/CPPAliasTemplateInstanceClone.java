/*******************************************************************************
 * Copyright (c) 2016 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;

/**
 * Delegating clone implementation for index classes implementing {@link ICPPAliasTemplateInstance} interface.
 */
public class CPPAliasTemplateInstanceClone extends CPPTypedefClone implements ICPPAliasTemplateInstance {
	public CPPAliasTemplateInstanceClone(ICPPAliasTemplateInstance original) {
		super(original);
	}

	@Override
	public Object clone() {
		return new CPPAliasTemplateInstanceClone(this);
	}

	private ICPPAliasTemplateInstance getDelegate() {
		return (ICPPAliasTemplateInstance) delegate;
	}

	@Override
	public ICPPAliasTemplate getTemplateDefinition() {
		return getDelegate().getTemplateDefinition();
	}

	@Override
	public ICPPTemplateArgument[] getTemplateArguments() {
		return getDelegate().getTemplateArguments();
	}

	@Override
	public boolean isExplicitSpecialization() {
		return false;
	}

	@Override
	public IBinding getSpecializedBinding() {
		return getDelegate().getSpecializedBinding();
	}

	@Override
	public ICPPTemplateParameterMap getTemplateParameterMap() {
		return getDelegate().getTemplateParameterMap();
	}
}
