/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
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

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCPPAliasTemplate extends CompositeCPPBinding implements ICPPAliasTemplate {
	public CompositeCPPAliasTemplate(ICompositesFactory cf, ICPPBinding delegate) {
		super(cf, delegate);
	}

	@Override
	public Object clone() {
		fail();
		return null;
	}

	@Override
	public IType getType() {
		IType type = ((ICPPAliasTemplate) rbinding).getType();
		return cf.getCompositeType(type);
	}

	@Override
	public boolean isSameType(IType type) {
		return ((ICPPAliasTemplate) rbinding).isSameType(type);
	}

	@Override
	public ICPPTemplateParameter[] getTemplateParameters() {
		return TemplateInstanceUtil.convert(cf, ((ICPPAliasTemplate) rbinding).getTemplateParameters());
	}
}
