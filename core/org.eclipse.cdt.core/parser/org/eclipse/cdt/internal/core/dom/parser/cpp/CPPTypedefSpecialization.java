/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;

/**
 * Specialization of a typedef in the context of a class-specialization.
 */
public class CPPTypedefSpecialization extends CPPSpecialization implements ITypedef, ITypeContainer {
	public static final int MAX_RESOLUTION_DEPTH = 5;
	public static final int MAX_TYPE_NESTING = 60;

	private IType fType;

	public CPPTypedefSpecialization(IBinding specialized, IBinding owner, ICPPTemplateParameterMap tpmap, IType type) {
		super(specialized, owner, tpmap);
		fType = type;
	}

	@Override
	public IType getType() {
		return fType;
	}

	@Override
	public Object clone() {
		IType t = null;
		try {
			t = (IType) super.clone();
		} catch (CloneNotSupportedException e) {
			// Not going to happen.
		}
		return t;
	}

	@Override
	public boolean isSameType(IType o) {
		if (o == this)
			return true;
		if (o instanceof ITypedef) {
			IType t = getType();
			if (t != null)
				return t.isSameType(((ITypedef) o).getType());
			return false;
		}

		IType t = getType();
		if (t != null)
			return t.isSameType(o);
		return false;
	}

	@Override
	public void setType(IType type) {
		fType = type;
	}
}
