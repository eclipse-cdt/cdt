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
 *    IBM - Initial API and implementation
 *    Bryan Wilkinson (QNX)
 *    Markus Schorn (Wind River Systems)
 *    Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;

/**
 * Represents a specialization of a class-template.
 */
public class CPPClassTemplateSpecialization extends CPPClassSpecialization
		implements ICPPClassTemplate, ICPPInternalClassTemplate {
	private ObjectMap instances;
	private ICPPDeferredClassInstance fDeferredInstance;
	private ICPPClassTemplatePartialSpecialization[] fPartialSpecs;
	private ICPPTemplateParameter[] fTemplateParameters;

	public CPPClassTemplateSpecialization(ICPPClassTemplate orig, ICPPClassSpecialization owner,
			ICPPTemplateParameterMap argumentMap) {
		super(orig, owner, argumentMap);
	}

	public void setTemplateParameters(ICPPTemplateParameter[] templateParameters) {
		fTemplateParameters = templateParameters;
	}

	@Override
	public ICPPClassTemplatePartialSpecialization[] getPartialSpecializations() {
		if (fPartialSpecs == null) {
			ICPPClassTemplate origTemplate = getSpecializedBinding();
			ICPPClassTemplatePartialSpecialization[] orig = origTemplate.getPartialSpecializations();
			ICPPClassTemplatePartialSpecialization[] spec = new ICPPClassTemplatePartialSpecialization[orig.length];
			ICPPClassSpecialization owner = getOwner();
			for (int i = 0; i < orig.length; i++) {
				spec[i] = (ICPPClassTemplatePartialSpecialization) owner.specializeMember(orig[i]);
			}
			fPartialSpecs = spec;
		}
		return fPartialSpecs;
	}

	@Override
	public ICPPTemplateParameter[] getTemplateParameters() {
		return fTemplateParameters;
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
	public void addPartialSpecialization(ICPPClassTemplatePartialSpecialization spec) {
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public IBinding resolveTemplateParameter(ICPPTemplateParameter param) {
		return param;
	}

	@Override
	public final ICPPDeferredClassInstance asDeferredInstance() {
		if (fDeferredInstance == null) {
			fDeferredInstance = CPPTemplates.createDeferredInstance(this);
		}
		return fDeferredInstance;
	}

	@Override
	public ICPPTemplateArgument getDefaultArgFromIndex(int paramPos) throws DOMException {
		return null;
	}

	@Override
	public ICPPClassSpecialization getOwner() {
		return (ICPPClassSpecialization) super.getOwner();
	}

	@Override
	public ICPPClassTemplate getSpecializedBinding() {
		return (ICPPClassTemplate) super.getSpecializedBinding();
	}
}
