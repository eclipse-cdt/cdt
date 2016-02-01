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
package org.eclipse.cdt.internal.core.index;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplateInstance;

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

	@Override
	public ICPPAliasTemplate getTemplateDefinition() {
		return ((ICPPAliasTemplateInstance) delegate).getTemplateDefinition();
	}
}
