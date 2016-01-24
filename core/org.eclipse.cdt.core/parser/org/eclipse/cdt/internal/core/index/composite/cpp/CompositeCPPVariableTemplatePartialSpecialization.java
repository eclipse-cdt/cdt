/*******************************************************************************
 * Copyright (c) 2015, 2016 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lukas Wegmann (IFS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariableTemplatePartialSpecialization;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

public class CompositeCPPVariableTemplatePartialSpecialization extends CompositeCPPVariableTemplate
		implements ICPPVariableTemplatePartialSpecialization {

	public CompositeCPPVariableTemplatePartialSpecialization(ICompositesFactory cf, ICPPVariableTemplatePartialSpecialization delegate) {
		super(cf, delegate);
	}

	@Override
	public ICPPTemplateDefinition getPrimaryTemplate() {
		return (ICPPTemplateDefinition) cf.getCompositeBinding(
				(IIndexFragmentBinding) ((ICPPVariableTemplatePartialSpecialization) rbinding).getPrimaryTemplate());
	}

	@Override
	public ICPPTemplateArgument[] getTemplateArguments() {
		return TemplateInstanceUtil.getTemplateArguments(cf, (ICPPVariableTemplatePartialSpecialization) rbinding);
	}
}
