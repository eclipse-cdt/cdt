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
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFieldTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateScope;

public class CPPFieldTemplate extends CPPVariableTemplate implements ICPPFieldTemplate {

	public CPPFieldTemplate(IASTName name) {
		super(name);
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
		IScope scope = getScope();
		if (scope instanceof ICPPTemplateScope) {
			try {
				scope = scope.getParent();
			} catch (DOMException e) {
				return null;
			}
		}
		if (scope instanceof ICPPClassScope) {
			return ((ICPPClassScope) scope).getClassType();
		}
		return null;
	}

	@Override
	public int getFieldPosition() {
		return CPPField.getFieldPosition(getName(), getClassOwner());
	}
}
