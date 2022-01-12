/*******************************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.Objects;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.parser.util.ObjectMap;

/**
 * The specialization of a friend function template in the context of a class specialization.
 */
public class CPPFunctionTemplateSpecialization extends CPPFunctionSpecialization
		implements ICPPFunctionTemplate, ICPPInternalTemplate {
	private ObjectMap instances;

	public CPPFunctionTemplateSpecialization(ICPPFunction original, IBinding owner,
			ICPPTemplateParameterMap argumentMap, ICPPFunctionType type, IType[] exceptionSpecs) {
		super(original, owner, argumentMap, type, exceptionSpecs);
	}

	@Override
	public ICPPTemplateParameter[] getTemplateParameters() {
		ICPPFunctionTemplate template = (ICPPFunctionTemplate) getSpecializedBinding();
		return template.getTemplateParameters();
	}

	@Override
	public synchronized final void addInstance(ICPPTemplateArgument[] arguments, ICPPTemplateInstance instance) {
		if (instances == null)
			instances = new ObjectMap(2);
		String key = ASTTypeUtil.getArgumentListString(arguments, true);
		instances.put(key, instance);
	}

	@Override
	public synchronized final ICPPTemplateInstance getInstance(ICPPTemplateArgument[] arguments) {
		if (instances != null) {
			String key = ASTTypeUtil.getArgumentListString(arguments, true);
			return (ICPPTemplateInstance) instances.get(key);
		}
		return null;
	}

	@Override
	public synchronized ICPPTemplateInstance[] getAllInstances() {
		if (instances != null) {
			ICPPTemplateInstance[] result = new ICPPTemplateInstance[instances.size()];
			for (int i = 0; i < instances.size(); i++) {
				result[i] = (ICPPTemplateInstance) instances.getAt(i);
			}
			return result;
		}
		return ICPPTemplateInstance.EMPTY_TEMPLATE_INSTANCE_ARRAY;
	}

	@Override
	public IBinding resolveTemplateParameter(ICPPTemplateParameter param) {
		return param;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null || !getClass().equals(obj.getClass()))
			return false;
		CPPFunctionTemplateSpecialization other = (CPPFunctionTemplateSpecialization) obj;
		return Objects.equals(getSpecializedBinding(), other.getSpecializedBinding())
				&& Objects.equals(getOwner(), other.getOwner())
				&& Objects.equals(getTemplateParameterMap(), other.getTemplateParameterMap());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getSpecializedBinding(), getOwner(), getTemplateParameterMap());
	}
}
