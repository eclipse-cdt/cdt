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

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;

/*
 * Represents a partially instantiated C++ class template, declaration of which is not yet available.
 *
 * @author Sergey Prigogin
 */
public class CPPUnknownClassInstance extends CPPUnknownClass implements ICPPInternalUnknownClassInstance {
	private ICPPClassTemplatePartialSpecialization[] partialSpecializations;
	private ObjectMap instances;
	private final IType[] arguments;

	public CPPUnknownClassInstance(ICPPInternalUnknown scopeBinding, IASTName name, IType[] arguments) {
		super(scopeBinding, name);
		this.arguments = arguments;
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

	public ICPPSpecialization deferredInstance(ObjectMap argMap, IType[] arguments) {
		ICPPSpecialization instance = getInstance(arguments);
		if (instance == null) {
			instance = new CPPDeferredClassInstance(this, argMap, arguments);
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
		return deferredInstance(null, arguments);
	}

	public IType[] getArguments() {
		return arguments;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalUnknown#resolveUnknown(org.eclipse.cdt.core.parser.util.ObjectMap)
	 */
	@Override
	public IBinding resolveUnknown(ObjectMap argMap) throws DOMException {
		IBinding result = super.resolveUnknown(argMap);
		if (result instanceof ICPPSpecialization && result instanceof ICPPTemplateDefinition) {
			IType[] newArgs = CPPTemplates.instantiateTypes(arguments, argMap);
			result = CPPTemplates.instantiateTemplate((ICPPTemplateDefinition) result, newArgs, null);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalUnknownClassType#resolvePartially(org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalUnknown, org.eclipse.cdt.core.parser.util.ObjectMap)
	 */
	@Override
	public IBinding resolvePartially(ICPPInternalUnknown parentBinding, ObjectMap argMap) {
		IType[] newArgs = CPPTemplates.instantiateTypes(arguments, argMap);
		return new CPPUnknownClassInstance(parentBinding, name, newArgs);
	}

	@Override
	public String toString() {
		return getName() + " <" + ASTTypeUtil.getTypeListString(arguments) + ">"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
