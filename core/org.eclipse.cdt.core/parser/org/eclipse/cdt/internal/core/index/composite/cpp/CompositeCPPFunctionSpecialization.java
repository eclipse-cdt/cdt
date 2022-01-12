/*******************************************************************************
 * Copyright (c) 2007, 2016 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

public class CompositeCPPFunctionSpecialization extends CompositeCPPFunction implements ICPPSpecialization {

	public CompositeCPPFunctionSpecialization(ICompositesFactory cf, ICPPFunction ft) {
		super(cf, ft);
	}

	@Override
	public IBinding getSpecializedBinding() {
		return TemplateInstanceUtil.getSpecializedBinding(cf, rbinding);
	}

	@Override
	public ICPPTemplateParameterMap getTemplateParameterMap() {
		IBinding owner = getOwner();
		if (owner instanceof ICPPSpecialization) {
			return ((ICPPSpecialization) owner).getTemplateParameterMap();
		}
		return CPPTemplateParameterMap.EMPTY;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(getName()).append(' ').append(ASTTypeUtil.getParameterTypeString(getType()));
		return result.toString();
	}

	@Override
	public IType[] getExceptionSpecification() {
		IType[] es = ((ICPPFunction) rbinding).getExceptionSpecification();
		if (es == null || es.length == 0)
			return es;

		IType[] result = new IType[es.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = cf.getCompositeType(result[i]);
		}
		return result;
	}
}
