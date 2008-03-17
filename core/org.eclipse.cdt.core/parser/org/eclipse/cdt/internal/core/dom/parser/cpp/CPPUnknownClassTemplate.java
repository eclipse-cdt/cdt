/*******************************************************************************
 * Copyright (c) 2008 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.ObjectMap;

/*
 * Represents a C++ class template for which we don't yet have a complete declaration.
 *
 * @author Sergey Prigogin
 */
public class CPPUnknownClassTemplate extends CPPUnknownClass
		implements ICPPClassTemplate, ICPPInternalClassTemplate {
	private ICPPClassTemplatePartialSpecialization[] partialSpecializations;
	private ObjectMap instances;

	public CPPUnknownClassTemplate(ICPPScope scope, IBinding scopeBinding, IASTName name) {
		super(scope, scopeBinding, name);
	}

	public ICPPClassTemplatePartialSpecialization[] getPartialSpecializations()
			throws DOMException {
		return partialSpecializations;
	}

	public ICPPTemplateParameter[] getTemplateParameters() throws DOMException {
		return ICPPTemplateParameter.EMPTY_TEMPLATE_PARAMETER_ARRAY;
	}

	public void addPartialSpecialization(ICPPClassTemplatePartialSpecialization spec) {
		partialSpecializations = (ICPPClassTemplatePartialSpecialization[])	ArrayUtil.append(
				ICPPClassTemplatePartialSpecialization.class, partialSpecializations, spec);
	}

	public void addSpecialization(IType[] arguments, ICPPSpecialization specialization) {
		if (arguments == null)
			return;
		for (int i = 0; i < arguments.length; i++) {
			if (arguments[i] == null)
				return;
		}
		if (instances == null)
			instances = new ObjectMap(2);
		instances.put(arguments, specialization);
	}

	public ICPPSpecialization deferredInstance(IType[] arguments) {
		ICPPSpecialization instance = getInstance(arguments);
		if (instance == null) {
			instance = new CPPDeferredClassInstance(this, arguments);
			addSpecialization(arguments, instance);
		}
		return instance;
	}

	public ICPPSpecialization getInstance(IType[] arguments) {
		if (instances == null)
			return null;
		
		for (int i = 0; i < instances.size(); i++) {
			IType[] args = (IType[]) instances.keyAt(i);
			if (args.length == arguments.length) {
				int j = 0;
				for (; j < args.length; j++) {
					if (!args[j].isSameType(arguments[j]))
						break;
				}
				if (j == args.length) {
					return (ICPPSpecialization) instances.getAt(i);
				}
			}
		}
		return null;
	}

	public IBinding instantiate(IType[] arguments) {
		return deferredInstance(arguments);
	}

	@Override
	public IBinding resolveUnknown(ObjectMap argMap) throws DOMException {
		IBinding result = super.resolveUnknown(argMap);
		if (result instanceof ICPPSpecialization) {
			ICPPSpecialization specialization = (ICPPSpecialization) result;
			IASTNode parent = name.getParent();
			if (parent instanceof ICPPASTTemplateId) {
				IBinding binding = ((ICPPASTTemplateId) parent).resolveBinding();
				if (binding instanceof ICPPInternalDeferredClassInstance) {
					// This is a hack to get proper arguments for the template instantiation.
					// A proper solution should probably be implemented inside
					// CPPTemplates.instantiateTemplate, but I don't know how to do it.
					// When making any changes to this code please make sure to run
					// AST2TemplateTests.testRebindPattern_214447* tests.
					IType type = ((ICPPInternalDeferredClassInstance) binding).instantiate(argMap);
					IType[] arguments = ((ICPPTemplateInstance) type).getArguments();
					ICPPTemplateDefinition template =
							(ICPPTemplateDefinition) specialization.getSpecializedBinding();
					result = CPPTemplates.instantiateTemplate(template, arguments, null);
				}
			}
		}
		return result;
	}
}
