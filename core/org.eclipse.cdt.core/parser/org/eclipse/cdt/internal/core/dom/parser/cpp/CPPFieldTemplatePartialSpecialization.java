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
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFieldTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;

/**
 * A partial specialization of a field template.
 */
public class CPPFieldTemplatePartialSpecialization extends CPPVariableTemplatePartialSpecialization
		implements ICPPFieldTemplate {

	public CPPFieldTemplatePartialSpecialization(IASTName name, ICPPTemplateArgument[] args) {
		super(name, args);
	}

	@Override
	public ICompositeType getCompositeTypeOwner() {
		return getClassOwner();
	}

	@Override
	public int getVisibility() {
		return VariableHelpers.getVisibility(this);
	}

	@Override
	public ICPPClassType getClassOwner() {
		ICPPClassScope scope = (ICPPClassScope) getScope();
		return scope.getClassType();
	}

	@Override
	public int getFieldPosition() {
		return ((ICPPFieldTemplate) getPrimaryTemplate()).getFieldPosition();
	}
}
