/*******************************************************************************
 * Copyright (c) 2013 Nathan Ridge.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nathan Ridge - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumerationSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;

/**
 * Binding for a specialization of an enumerator.
 */
public class CPPEnumeratorSpecialization extends CPPSpecialization implements IEnumerator {
	private final IValue fValue;

	public CPPEnumeratorSpecialization(IEnumerator specialized, ICPPEnumerationSpecialization owner,
			ICPPTemplateParameterMap argumentMap, IValue value) {
		super(specialized, owner, argumentMap);
		fValue = value;
	}

	@Override
	public ICPPEnumerationSpecialization getOwner() {
		return (ICPPEnumerationSpecialization) super.getOwner();
	}

	@Override
	public IType getType() {
		return getOwner();
	}

	@Override
	public IValue getValue() {
		return fValue;
	}
}
