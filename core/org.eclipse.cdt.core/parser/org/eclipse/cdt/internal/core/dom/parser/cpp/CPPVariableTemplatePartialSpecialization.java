/*******************************************************************************
 * Copyright (c) 2015, 2016 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lukas Wegmann (IFS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariableTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariableTemplatePartialSpecialization;

/**
 * A partial specialization of a variable template.
 */
public class CPPVariableTemplatePartialSpecialization extends CPPVariableTemplate
		implements ICPPVariableTemplatePartialSpecialization {
	private final ICPPTemplateArgument[] arguments;

	public CPPVariableTemplatePartialSpecialization(IASTName name, ICPPTemplateArgument[] args) {
		super(name);
		arguments = args;
	}

	@Override
	public ICPPTemplateDefinition getPrimaryTemplate() {
		ICPPASTTemplateId id = (ICPPASTTemplateId) getTemplateName();
		return (ICPPVariableTemplate) id.getTemplateName().resolveBinding();
	}

	@Override
	public ICPPTemplateArgument[] getTemplateArguments() {
		return arguments;
	}
}
