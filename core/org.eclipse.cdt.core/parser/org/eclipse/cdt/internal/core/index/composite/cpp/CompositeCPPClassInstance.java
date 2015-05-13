/*******************************************************************************
 * Copyright (c) 2007, 2010 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

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

	@Override
	@Deprecated
	public IType[] getArguments() {
		return TemplateInstanceUtil.getArguments(cf, (ICPPTemplateInstance) rbinding);
	}
}
