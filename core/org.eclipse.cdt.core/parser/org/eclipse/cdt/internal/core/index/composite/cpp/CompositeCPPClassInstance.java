/*******************************************************************************
 * Copyright (c) 2007, 2015 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

/**
 * The result of instantiating a class template or an explicit specialization of a class template.
 */
public class CompositeCPPClassInstance extends CompositeCPPClassSpecialization implements ICPPTemplateInstance {

	public CompositeCPPClassInstance(ICompositesFactory cf, ICPPClassType rbinding) {
		super(cf, rbinding);
	}

	@Override
	public ICPPTemplateDefinition getTemplateDefinition() {
		return TemplateInstanceUtil.getTemplateDefinition(cf, rbinding);
	}

	@Override
	public ICPPTemplateArgument[] getTemplateArguments() {
		return TemplateInstanceUtil.getTemplateArguments(cf, (ICPPTemplateInstance) rbinding);
	}

	@Override
	public ICPPTemplateParameterMap getTemplateParameterMap() {
		return TemplateInstanceUtil.getTemplateParameterMap(cf, (ICPPTemplateInstance) rbinding);
	}

	@Override
	public boolean isExplicitSpecialization() {
		return ((ICPPTemplateInstance) rbinding).isExplicitSpecialization();
	}
}
