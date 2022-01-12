/*******************************************************************************
 * Copyright (c) 2009, 2016 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecializationSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;

/**
 * Represents a specialization of a partial class-template specialization
 */
public class CPPClassTemplatePartialSpecializationSpecialization extends CPPClassSpecialization
		implements ICPPClassTemplatePartialSpecializationSpecialization, ICPPInternalClassTemplate {
	private ObjectMap instances;
	private ICPPDeferredClassInstance fDeferredInstance;
	private final ICPPClassTemplate fClassTemplate;
	private final ICPPTemplateArgument[] fArguments;

	public CPPClassTemplatePartialSpecializationSpecialization(ICPPClassTemplatePartialSpecialization orig,
			ICPPTemplateParameterMap argumentMap, ICPPClassTemplate template, ICPPTemplateArgument[] args)
			throws DOMException {
		super(orig, template.getOwner(), argumentMap);
		fClassTemplate = template;
		fArguments = args;
	}

	@Override
	public ICPPTemplateParameter[] getTemplateParameters() {
		ICPPClassTemplatePartialSpecialization template = (ICPPClassTemplatePartialSpecialization) getSpecializedBinding();
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
	public ICPPDeferredClassInstance asDeferredInstance() {
		if (fDeferredInstance == null) {
			fDeferredInstance = CPPTemplates.createDeferredInstance(this);
		}
		return fDeferredInstance;
	}

	@Override
	public ICPPClassTemplate getPrimaryClassTemplate() {
		return fClassTemplate;
	}

	@Override
	public ICPPTemplateArgument[] getTemplateArguments() {
		return fArguments;
	}

	@Override
	public void addPartialSpecialization(ICPPClassTemplatePartialSpecialization spec) {
	}

	@Override
	public ICPPClassTemplatePartialSpecialization[] getPartialSpecializations() {
		return ICPPClassTemplatePartialSpecialization.EMPTY_ARRAY;
	}

	@Override
	public boolean isSameType(IType type) {
		if (type == this)
			return true;
		if (type instanceof ITypedef)
			return type.isSameType(this);

		if (type instanceof ICPPClassTemplatePartialSpecializationSpecialization) {
			return CPPClassTemplatePartialSpecialization.isSamePartialClassSpecialization(this,
					(ICPPClassTemplatePartialSpecializationSpecialization) type);
		}
		return false;
	}

	@Override
	public String toString() {
		return super.toString() + ASTTypeUtil.getArgumentListString(getTemplateArguments(), true);
	}

	@Override
	public ICPPTemplateArgument getDefaultArgFromIndex(int paramPos) throws DOMException {
		// No default arguments for partial specializations
		return null;
	}

	@Override
	public ICPPTemplateDefinition getPrimaryTemplate() {
		return getPrimaryClassTemplate();
	}
}
