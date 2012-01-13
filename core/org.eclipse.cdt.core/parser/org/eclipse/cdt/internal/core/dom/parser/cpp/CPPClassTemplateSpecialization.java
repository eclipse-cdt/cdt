/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Bryan Wilkinson (QNX)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;

/**
 * Represents a specialization of a class-template
 */
public class CPPClassTemplateSpecialization extends CPPClassSpecialization
		implements ICPPClassTemplate, ICPPInternalClassTemplate {

	private ObjectMap instances = null;
	private ICPPDeferredClassInstance fDeferredInstance;
	private ICPPClassTemplatePartialSpecialization[] fPartialSpecs;

	public CPPClassTemplateSpecialization(ICPPClassTemplate orig, ICPPClassType owner, ICPPTemplateParameterMap argumentMap) {
		super(orig, owner, argumentMap);
	}

	@Override
	public ICPPClassTemplatePartialSpecialization[] getPartialSpecializations() {
		if (fPartialSpecs == null) {
			ICPPClassTemplate origTemplate= (ICPPClassTemplate) getSpecializedBinding();
			ICPPClassTemplatePartialSpecialization[] orig = origTemplate.getPartialSpecializations();
			ICPPClassTemplatePartialSpecialization[] spec = new ICPPClassTemplatePartialSpecialization[orig.length];
			for (int i = 0; i < orig.length; i++) {
				spec[i]= (ICPPClassTemplatePartialSpecialization) specializeMember(orig[i]);
			}
			fPartialSpecs = spec;
		}
		return fPartialSpecs;
	}

	@Override
	public ICPPTemplateParameter[] getTemplateParameters() {
		// mstodo if we specialize the template parameters (because of its default values), it will
		// be less error prone to use the defaults.
		ICPPClassTemplate template = (ICPPClassTemplate) getSpecializedBinding();
		return template.getTemplateParameters();
	}

	@Override
	public synchronized final void addInstance(ICPPTemplateArgument[] arguments, ICPPTemplateInstance instance) {
		if (instances == null)
			instances = new ObjectMap(2);
		String key= ASTTypeUtil.getArgumentListString(arguments, true);
		instances.put(key, instance);
	}

	@Override
	public synchronized final ICPPTemplateInstance getInstance(ICPPTemplateArgument[] arguments) {
		if (instances != null) {
			String key= ASTTypeUtil.getArgumentListString(arguments, true);
			return (ICPPTemplateInstance) instances.get(key);
		}
		return null;
	}

	@Override
	public synchronized ICPPTemplateInstance[] getAllInstances() {
		if (instances != null) {
			ICPPTemplateInstance[] result= new ICPPTemplateInstance[instances.size()];
			for (int i=0; i < instances.size(); i++) {
				result[i]= (ICPPTemplateInstance) instances.getAt(i);
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
			fDeferredInstance= CPPTemplates.createDeferredInstance(this);
		}
		return fDeferredInstance;
	}
	
	@Override
	public ICPPTemplateArgument getDefaultArgFromIndex(int paramPos) throws DOMException {
		return null;
	}
}
