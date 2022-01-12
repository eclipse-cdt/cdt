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

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFieldTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.parser.util.ObjectMap;

/**
 * A field template of a specialized class template.
 */
public class CPPFieldTemplateSpecialization extends CPPFieldSpecialization
		implements ICPPFieldTemplate, ICPPInternalTemplate {
	private ICPPTemplateParameter[] templateParameters;
	private ObjectMap instances;

	public CPPFieldTemplateSpecialization(IBinding orig, ICPPClassType owner, ICPPTemplateParameterMap tpmap,
			IType type, IValue value) {
		super(orig, owner, tpmap, type, value);
	}

	@Override
	public ICPPPartialSpecialization[] getPartialSpecializations() {
		// Partial specializations of field template specializations is not specified
		// and I've found no working example on recent compiler implementations
		// see also http://wg21.cmeerw.net/cwg/issue1711
		return ICPPPartialSpecialization.EMPTY_ARRAY;
	}

	public void setTemplateParameters(ICPPTemplateParameter[] params) {
		templateParameters = params;
	}

	@Override
	public ICPPTemplateParameter[] getTemplateParameters() {
		return templateParameters;
	}

	@Override
	public IBinding resolveTemplateParameter(ICPPTemplateParameter param) {
		return param;
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
	public ICPPTemplateInstance[] getAllInstances() {
		if (instances != null) {
			ICPPTemplateInstance[] result = new ICPPTemplateInstance[instances.size()];
			for (int i = 0; i < instances.size(); i++) {
				result[i] = (ICPPTemplateInstance) instances.getAt(i);
			}
			return result;
		}
		return ICPPTemplateInstance.EMPTY_TEMPLATE_INSTANCE_ARRAY;
	}
}
