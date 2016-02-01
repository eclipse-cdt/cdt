/*******************************************************************************
 * Copyright (c) 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.internal.core.index.CPPAliasTemplateInstanceClone;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCPPAliasTemplateInstance extends CompositeCPPTypedef implements ICPPAliasTemplateInstance {
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
}
