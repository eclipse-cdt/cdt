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
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.internal.core.index.CPPAliasTemplateInstanceClone;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCPPAliasTemplateInstance extends CompositeCPPTypedefSpecialization implements ICPPAliasTemplateInstance {
	public CompositeCPPAliasTemplateInstance(ICompositesFactory cf, ICPPBinding delegate) {
		super(cf, delegate);
	}

	@Override
	public ICPPAliasTemplate getTemplateDefinition() {
		ICPPAliasTemplate templateDefinition = ((ICPPAliasTemplateInstance) rbinding).getTemplateDefinition();
		return (ICPPAliasTemplate) cf.getCompositeBinding((IIndexFragmentBinding) templateDefinition);
	}

	@Override
	public Object clone() {
		return new CPPAliasTemplateInstanceClone(this);
	}

	@Override
	public ICPPTemplateArgument[] getTemplateArguments() {
		return TemplateInstanceUtil.getTemplateArguments(cf, (ICPPTemplateInstance) rbinding);
	}

	@Override
	public boolean isExplicitSpecialization() {
		return false;
	}
}
