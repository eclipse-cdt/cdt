/*******************************************************************************
 * Copyright (c) 2017 Nathan Ridge.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;

/**
 * Specialization of an alias template.
 */
public class CPPAliasTemplateSpecialization extends CPPSpecialization implements ICPPAliasTemplate {
	private ICPPTemplateParameter[] fParameters;
	private IType fAliasedType;

	public CPPAliasTemplateSpecialization(ICPPAliasTemplate specialized, IBinding owner,
			ICPPTemplateParameterMap argumentMap, IType aliasedType) {
		super(specialized, owner, argumentMap);
		fAliasedType = aliasedType;
	}

	public void setTemplateParameters(ICPPTemplateParameter[] parameters) {
		fParameters = parameters;
	}

	@Override
	public boolean isSameType(IType type) {
		if (type == null) {
			return false;
		}
		return type.isSameType(fAliasedType);
	}

	@Override
	public ICPPTemplateParameter[] getTemplateParameters() {
		return fParameters;
	}

	@Override
	public IType getType() {
		return fAliasedType;
	}

	@Override
	public Object clone() {
		IType t = null;
		try {
			t = (IType) super.clone();
		} catch (CloneNotSupportedException e) {
			// Not going to happen
		}
		return t;
	}
}
