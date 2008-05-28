/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;

/**
 * @author aniefer
 *
 */
public class CPPClassTemplateSpecialization extends CPPClassSpecialization
		implements ICPPClassTemplate, ICPPInternalClassTemplate {

	private ObjectMap instances = null;

	/**
	 * @param specialized
	 * @param scope
	 * @param argumentMap
	 */
	public CPPClassTemplateSpecialization(IBinding specialized,	ICPPScope scope,
			ObjectMap argumentMap) {
		super(specialized, scope, argumentMap);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate#getPartialSpecializations()
	 */
	public ICPPClassTemplatePartialSpecialization[] getPartialSpecializations() throws DOMException {
		return ((ICPPClassTemplate) getSpecializedBinding()).getPartialSpecializations();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition#getTemplateParameters()
	 */
	public ICPPTemplateParameter[] getTemplateParameters() throws DOMException {
		ICPPClassTemplate template = (ICPPClassTemplate) getSpecializedBinding();
		return template.getTemplateParameters();
	}

	public void addSpecialization(IType[] arguments, ICPPSpecialization specialization) {
		if (instances == null)
			instances = new ObjectMap(2);
		instances.put(arguments, specialization);
	}

	public ICPPSpecialization getInstance(IType[] arguments) {
		if (instances == null)
			return null;

		int found = -1;
		for (int i = 0; i < instances.size(); i++) {
			IType[] args = (IType[]) instances.keyAt(i);
			if (args.length == arguments.length) {
				int j = 0;
				for(; j < args.length; j++) {
					if (!CPPTemplates.isSameTemplateArgument(args[j], arguments[j]))
						break;
				}
				if (j == args.length) {
					found = i;
					break;
				}
			}
		}
		if (found != -1) {
			return (ICPPSpecialization) instances.getAt(found);
		}
		return null;
	}

	public IBinding instantiate(IType[] arguments) {
		ICPPTemplateDefinition template = null;

		try {
			template = CPPTemplates.matchTemplatePartialSpecialization(this, arguments);
		} catch (DOMException e) {
			return e.getProblem();
		}

		if (template instanceof IProblemBinding) {
			return template;
		}
		if (template != null && template instanceof ICPPClassTemplatePartialSpecialization) {
			return ((ICPPInternalTemplateInstantiator) template).instantiate(arguments);
		}

		return CPPTemplates.instantiateTemplate(this, arguments, argumentMap);
	}

	public ICPPSpecialization deferredInstance(ObjectMap argMap, IType[] arguments) {
		ICPPSpecialization instance = getInstance(arguments);
		if (instance == null) {
			instance = new CPPDeferredClassInstance(this, argMap, arguments);
			addSpecialization(arguments, instance);
		}
		return instance;
	}

	public void addPartialSpecialization(ICPPClassTemplatePartialSpecialization spec) {
		//should not occur
	}

	@Override
	public String toString() {
		return getName();
	}
}
