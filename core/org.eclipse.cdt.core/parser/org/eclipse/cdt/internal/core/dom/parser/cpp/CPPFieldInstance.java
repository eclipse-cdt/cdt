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
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFieldTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;

/**
 * An instance of a field template.
 */
public class CPPFieldInstance extends CPPVariableInstance implements ICPPField {

	public CPPFieldInstance(IBinding specialized, IBinding owner, ICPPTemplateParameterMap argumentMap,
			ICPPTemplateArgument[] args, IType tpe, IValue value) {
		super(specialized, owner, argumentMap, args, tpe, value);
	}

	@Override
	public ICompositeType getCompositeTypeOwner() {
		return getClassOwner();
	}

	@Override
	public int getVisibility() {
		return ((ICPPFieldTemplate) getSpecializedBinding()).getVisibility();
	}

	@Override
	public ICPPClassType getClassOwner() {
		try {
			ICPPClassScope scope = (ICPPClassScope) getScope();
			return scope.getClassType();
		} catch (DOMException e) {
			return null;
		}
	}

	@Override
	public int getFieldPosition() {
		return ((ICPPFieldTemplate) getSpecializedBinding()).getFieldPosition();
	}
}
